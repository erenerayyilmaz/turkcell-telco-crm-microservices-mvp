package com.turkcell.notificationservice.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.saga.SubscriptionReactivated;
import com.turkcell.commonlib.saga.SubscriptionSuspended;
import com.turkcell.commonlib.saga.SubscriptionTerminated;
import com.turkcell.notificationservice.entity.Notification;
import com.turkcell.notificationservice.entity.ProcessedEvent;
import com.turkcell.notificationservice.repository.NotificationRepository;
import com.turkcell.notificationservice.repository.ProcessedEventRepository;

/**
 * Abonelik yasam dongusu bildirimleri (G4, FR-14 / docx §8.4).
 * Inbox idempotency + yazim tek transaction; kanal SMS (mock).
 *  - SubscriptionSuspended   -> "SUBSCRIPTION_SUSPENDED"   (hattiniz askiya alindi)
 *  - SubscriptionReactivated -> "SUBSCRIPTION_REACTIVATED" (hattiniz yeniden acildi)
 *  - SubscriptionTerminated  -> "SUBSCRIPTION_TERMINATED"  (hattiniz kapatildi)
 */
@Service
public class SubscriptionEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventHandler.class);
    private static final String TPL_SUSPENDED = "SUBSCRIPTION_SUSPENDED";
    private static final String TPL_REACTIVATED = "SUBSCRIPTION_REACTIVATED";
    private static final String TPL_TERMINATED = "SUBSCRIPTION_TERMINATED";

    private final ProcessedEventRepository processedEventRepository;
    private final NotificationRepository notificationRepository;

    public SubscriptionEventHandler(ProcessedEventRepository processedEventRepository,
                                    NotificationRepository notificationRepository) {
        this.processedEventRepository = processedEventRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void handleSuspended(SubscriptionSuspended event) {
        send(event.eventId(), event.customerId(), TPL_SUSPENDED,
                "msisdn=" + event.msisdn() + " sebep=" + event.reason());
    }

    @Transactional
    public void handleReactivated(SubscriptionReactivated event) {
        send(event.eventId(), event.customerId(), TPL_REACTIVATED, "msisdn=" + event.msisdn());
    }

    @Transactional
    public void handleTerminated(SubscriptionTerminated event) {
        send(event.eventId(), event.customerId(), TPL_TERMINATED,
                "msisdn=" + event.msisdn() + " sebep=" + event.reason());
    }

    private void send(UUID eventId, UUID customerId, String templateCode, String detail) {
        if (processedEventRepository.existsById(eventId)) {
            log.info("notification: event zaten islendi, atlaniyor. eventId={}", eventId);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(customerId);
        notification.setTemplateCode(templateCode);
        notification.setChannel("SMS");
        notification.setStatus("SENT");
        notification.setSentAt(Instant.now());
        notificationRepository.save(notification);

        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventId(eventId);
        processed.setProcessedAt(Instant.now());
        processedEventRepository.save(processed);

        log.info("notification: {} gonderildi (SMS) -> customer={} {}", templateCode, customerId, detail);
    }
}
