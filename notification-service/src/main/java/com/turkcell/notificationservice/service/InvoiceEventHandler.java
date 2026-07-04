package com.turkcell.notificationservice.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.saga.InvoiceGenerated;
import com.turkcell.commonlib.saga.InvoicePaid;
import com.turkcell.commonlib.saga.InvoicePaymentFailed;
import com.turkcell.notificationservice.entity.Notification;
import com.turkcell.notificationservice.entity.ProcessedEvent;
import com.turkcell.notificationservice.repository.NotificationRepository;
import com.turkcell.notificationservice.repository.ProcessedEventRepository;

/**
 * Fatura yasam dongusu bildirimleri (G2, docx senaryo 14.2 / §8.8).
 * Inbox idempotency + yazim tek transaction; kanal EMAIL (mock).
 *  - InvoiceGenerated     -> "INVOICE_GENERATED"      (faturaniz kesildi)
 *  - InvoicePaid          -> "INVOICE_PAID"           (odemeniz alindi)
 *  - InvoicePaymentFailed -> "INVOICE_PAYMENT_FAILED" (odeme basarisiz; dunning konusu)
 */
@Service
public class InvoiceEventHandler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventHandler.class);
    private static final String TPL_GENERATED = "INVOICE_GENERATED";
    private static final String TPL_PAID = "INVOICE_PAID";
    private static final String TPL_PAYMENT_FAILED = "INVOICE_PAYMENT_FAILED";

    private final ProcessedEventRepository processedEventRepository;
    private final NotificationRepository notificationRepository;

    public InvoiceEventHandler(ProcessedEventRepository processedEventRepository,
                               NotificationRepository notificationRepository) {
        this.processedEventRepository = processedEventRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void handleGenerated(InvoiceGenerated event) {
        send(event.eventId(), event.customerId(), TPL_GENERATED,
                "fatura kesildi (invoice=" + event.invoiceId() + ", tutar=" + event.grandTotal()
                        + " " + event.currency() + ", vade=" + event.dueDate() + ")");
    }

    @Transactional
    public void handlePaid(InvoicePaid event) {
        send(event.eventId(), event.customerId(), TPL_PAID,
                "odeme alindi (invoice=" + event.invoiceId() + ", tutar=" + event.amount()
                        + " " + event.currency() + ")");
    }

    @Transactional
    public void handlePaymentFailed(InvoicePaymentFailed event) {
        send(event.eventId(), event.customerId(), TPL_PAYMENT_FAILED,
                "odeme basarisiz (invoice=" + event.invoiceId() + ", sebep=" + event.reason() + ")");
    }

    private void send(UUID eventId, UUID customerId, String templateCode, String detail) {
        if (processedEventRepository.existsById(eventId)) {
            log.info("notification: event zaten islendi, atlaniyor. eventId={}", eventId);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(customerId);
        notification.setTemplateCode(templateCode);
        notification.setChannel("EMAIL");
        notification.setStatus("SENT");
        notification.setSentAt(Instant.now());
        notificationRepository.save(notification);

        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventId(eventId);
        processed.setProcessedAt(Instant.now());
        processedEventRepository.save(processed);

        log.info("notification: {} gonderildi (EMAIL) -> customer={} {}", templateCode, customerId, detail);
    }
}
