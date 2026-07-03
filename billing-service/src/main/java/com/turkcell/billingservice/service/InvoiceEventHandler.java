package com.turkcell.billingservice.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.billingservice.entity.ProcessedEvent;
import com.turkcell.billingservice.repository.InvoiceRepository;
import com.turkcell.billingservice.repository.ProcessedEventRepository;
import com.turkcell.commonlib.saga.InvoicePaid;
import com.turkcell.commonlib.saga.InvoicePaymentFailed;

/** Fatura tahsilat reply isleyicisi. Inbox kontrolu + durum guncelleme AYNI transaction'da. */
@Service
public class InvoiceEventHandler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventHandler.class);

    private final ProcessedEventRepository processedEventRepository;
    private final InvoiceRepository invoiceRepository;

    public InvoiceEventHandler(ProcessedEventRepository processedEventRepository,
                               InvoiceRepository invoiceRepository) {
        this.processedEventRepository = processedEventRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public void onPaid(InvoicePaid event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("billing: event zaten islendi, atlaniyor. eventId={}", event.eventId());
            return;
        }
        invoiceRepository.findById(event.invoiceId()).ifPresentOrElse(invoice -> {
            invoice.setStatus("PAID");
            invoiceRepository.save(invoice);
            log.info("billing: invoice={} PAID (payment={})", event.invoiceId(), event.paymentId());
        }, () -> log.warn("billing: InvoicePaid ama fatura yok: {}", event.invoiceId()));
        markProcessed(event.eventId());
    }

    @Transactional
    public void onPaymentFailed(InvoicePaymentFailed event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("billing: event zaten islendi, atlaniyor. eventId={}", event.eventId());
            return;
        }
        invoiceRepository.findById(event.invoiceId()).ifPresentOrElse(invoice -> {
            invoice.setStatus("PAYMENT_FAILED");
            invoiceRepository.save(invoice);
            log.warn("billing: invoice={} tahsilat BASARISIZ: {}", event.invoiceId(), event.reason());
        }, () -> log.warn("billing: InvoicePaymentFailed ama fatura yok: {}", event.invoiceId()));
        markProcessed(event.eventId());
    }

    private void markProcessed(UUID eventId) {
        ProcessedEvent pe = new ProcessedEvent();
        pe.setEventId(eventId);
        pe.setProcessedAt(Instant.now());
        processedEventRepository.save(pe);
    }
}
