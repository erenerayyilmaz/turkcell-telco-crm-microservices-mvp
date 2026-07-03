package com.turkcell.billingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.turkcell.billingservice.entity.BillCycle;
import com.turkcell.billingservice.entity.Invoice;
import com.turkcell.billingservice.entity.InvoiceLine;
import com.turkcell.billingservice.repository.BillCycleRepository;
import com.turkcell.billingservice.repository.InvoiceLineRepository;
import com.turkcell.billingservice.repository.InvoiceRepository;
import com.turkcell.billingservice.saga.OutboxWriter;
import com.turkcell.commonlib.saga.ChargeInvoiceCommand;
import com.turkcell.commonlib.saga.SagaTopics;

/**
 * Bill-run fatura matematigi + dongu ilerletme + auto-pay komutu.
 * (Canli e2e ile dogrulanan degerlerin regresyon guvencesi: 249.90 -> KDV %20 -> 299.88.)
 */
class BillRunServiceTest {

    private BillCycleRepository billCycleRepository;
    private InvoiceRepository invoiceRepository;
    private InvoiceLineRepository invoiceLineRepository;
    private OutboxWriter outbox;
    private BillRunService service;

    @BeforeEach
    void setUp() {
        billCycleRepository = mock(BillCycleRepository.class);
        invoiceRepository = mock(InvoiceRepository.class);
        invoiceLineRepository = mock(InvoiceLineRepository.class);
        outbox = mock(OutboxWriter.class);
        service = new BillRunService(billCycleRepository, invoiceRepository, invoiceLineRepository, outbox);

        // JPA save id atar; unit testte taklit edilir.
        when(invoiceRepository.save(any())).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });
    }

    @Test
    void issuesInvoiceWithTaxAdvancesCycleAndEnqueuesAutoPay() {
        BillCycle cycle = new BillCycle();
        cycle.setId(UUID.randomUUID());
        cycle.setCustomerId(UUID.randomUUID());
        cycle.setSubscriptionId(UUID.randomUUID());
        cycle.setMonthlyFee(new BigDecimal("249.90"));
        cycle.setCurrency("TRY");
        cycle.setNextRunDate(LocalDate.of(2026, 7, 3));
        when(billCycleRepository.findDue(any(), anyInt())).thenReturn(List.of(cycle));

        service.runDueCycles();

        // Fatura: donem, KDV %20, vade +15 gun, ISSUED.
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice invoice = invoiceCaptor.getValue();
        assertThat(invoice.getSubTotal()).isEqualByComparingTo("249.90");
        assertThat(invoice.getTax()).isEqualByComparingTo("49.98");
        assertThat(invoice.getGrandTotal()).isEqualByComparingTo("299.88");
        assertThat(invoice.getStatus()).isEqualTo("ISSUED");
        assertThat(invoice.getPeriodStart()).isEqualTo(LocalDate.of(2026, 6, 3));
        assertThat(invoice.getPeriodEnd()).isEqualTo(LocalDate.of(2026, 7, 3));
        assertThat(invoice.getDueDate()).isEqualTo(LocalDate.of(2026, 7, 18));

        // Kalem: 1 x aylik ucret.
        ArgumentCaptor<InvoiceLine> lineCaptor = ArgumentCaptor.forClass(InvoiceLine.class);
        verify(invoiceLineRepository).save(lineCaptor.capture());
        assertThat(lineCaptor.getValue().getLineTotal()).isEqualByComparingTo("249.90");
        assertThat(lineCaptor.getValue().getInvoiceId()).isEqualTo(invoice.getId());

        // Auto-pay komutu: grandTotal ile payment-commands'a.
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(outbox).enqueue(eq(SagaTopics.PAYMENT_COMMANDS), eq("ChargeInvoiceCommand"),
                eq(invoice.getId()), payloadCaptor.capture());
        ChargeInvoiceCommand cmd = (ChargeInvoiceCommand) payloadCaptor.getValue();
        assertThat(cmd.invoiceId()).isEqualTo(invoice.getId());
        assertThat(cmd.amount()).isEqualByComparingTo("299.88");
        assertThat(cmd.currency()).isEqualTo("TRY");

        // Dongu +1 ay ilerledi.
        assertThat(cycle.getNextRunDate()).isEqualTo(LocalDate.of(2026, 8, 3));
        verify(billCycleRepository).save(cycle);
    }

    @Test
    void noDueCyclesDoesNothing() {
        when(billCycleRepository.findDue(any(), anyInt())).thenReturn(List.of());
        service.runDueCycles();
        verify(invoiceRepository, org.mockito.Mockito.never()).save(any());
        verify(outbox, org.mockito.Mockito.never()).enqueue(any(), any(), any(), any());
    }
}
