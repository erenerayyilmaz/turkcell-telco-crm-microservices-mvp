package com.turkcell.billingservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * Aylik bill-run (Faz 2 recurring billing): vadesi gelen bill_cycles satirlari icin
 * fatura keser (ISSUED) ve otomatik tahsilat komutunu (ChargeInvoiceCommand) outbox'a yazar;
 * OutboxPoller payment-commands'a publish eder, cevap invoice-events'ten doner.
 *
 * Fatura kesimi + outbox + dongu ilerletme TEK transaction'dadir; findDue
 * FOR UPDATE SKIP LOCKED kullandigi icin coklu instance guvenlidir.
 * Demo icin 1 dk'da bir tarama yapilir (prod'da gunluk cron uygundur).
 */
@Service
public class BillRunService {

    private static final Logger log = LoggerFactory.getLogger(BillRunService.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.20"); // KDV %20
    private static final int DUE_DAYS = 15;
    private static final int BATCH_LIMIT = 100;

    private final BillCycleRepository billCycleRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final OutboxWriter outbox;

    public BillRunService(BillCycleRepository billCycleRepository,
                          InvoiceRepository invoiceRepository,
                          InvoiceLineRepository invoiceLineRepository,
                          OutboxWriter outbox) {
        this.billCycleRepository = billCycleRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.outbox = outbox;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void runDueCycles() {
        List<BillCycle> due = billCycleRepository.findDue(LocalDate.now(), BATCH_LIMIT);
        for (BillCycle cycle : due) {
            issueInvoice(cycle);
        }
        if (!due.isEmpty()) {
            log.info("bill-run: {} dongu islendi", due.size());
        }
    }

    private void issueInvoice(BillCycle cycle) {
        // Donem: bir onceki fatura tarihinden bu fatura tarihine (yarim acik aralik).
        LocalDate periodEnd = cycle.getNextRunDate();
        LocalDate periodStart = periodEnd.minusMonths(1);

        BigDecimal subTotal = cycle.getMonthlyFee().setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subTotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subTotal.add(tax);

        Invoice invoice = new Invoice();
        invoice.setCustomerId(cycle.getCustomerId());
        invoice.setSubscriptionId(cycle.getSubscriptionId());
        invoice.setBillCycleId(cycle.getId());
        invoice.setPeriodStart(periodStart);
        invoice.setPeriodEnd(periodEnd);
        invoice.setSubTotal(subTotal);
        invoice.setTax(tax);
        invoice.setGrandTotal(grandTotal);
        invoice.setStatus("ISSUED");
        invoice.setDueDate(periodEnd.plusDays(DUE_DAYS));
        invoice.setIssuedAt(Instant.now());
        invoice = invoiceRepository.save(invoice);

        InvoiceLine line = new InvoiceLine();
        line.setInvoiceId(invoice.getId());
        line.setDescription("Aylik abonelik bedeli (" + periodStart + " - " + periodEnd + ")");
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(subTotal);
        line.setLineTotal(subTotal);
        invoiceLineRepository.save(line);

        // Otomatik tahsilat: payment ChargeInvoiceCommand'i isler, reply invoice-events'e gelir.
        outbox.enqueue(SagaTopics.PAYMENT_COMMANDS, "ChargeInvoiceCommand", invoice.getId(),
                new ChargeInvoiceCommand(UUID.randomUUID(), invoice.getId(),
                        invoice.getCustomerId(), grandTotal, cycle.getCurrency()));

        // Donguyu bir sonraki aya ilerlet.
        cycle.setNextRunDate(periodEnd.plusMonths(1));
        billCycleRepository.save(cycle);

        log.info("bill-run: fatura kesildi invoice={} customer={} tutar={} {} (donem {} - {})",
                invoice.getId(), invoice.getCustomerId(), grandTotal, cycle.getCurrency(), periodStart, periodEnd);
    }
}
