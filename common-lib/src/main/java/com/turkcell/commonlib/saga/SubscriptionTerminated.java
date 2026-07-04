package com.turkcell.commonlib.saga;

import java.util.UUID;

/**
 * Domain event: subscription -> dis dunya ({@link SagaTopics#SUBSCRIPTION_EVENTS}).
 * Abonelik TERMINATED oldugunda yayinlanir (G4, FR-14 / docx §8.4); MSISDN havuza
 * (FREE) doner, notification "hattiniz kapatildi" SMS'i atar.
 */
public record SubscriptionTerminated(
        UUID eventId,
        UUID subscriptionId,
        UUID customerId,
        String msisdn,
        String reason) {
}
