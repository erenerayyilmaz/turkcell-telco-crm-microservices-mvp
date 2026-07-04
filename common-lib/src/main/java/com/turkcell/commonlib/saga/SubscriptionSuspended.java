package com.turkcell.commonlib.saga;

import java.util.UUID;

/**
 * Domain event: subscription -> dis dunya ({@link SagaTopics#SUBSCRIPTION_EVENTS}).
 * Abonelik ACTIVE -> SUSPENDED oldugunda yayinlanir (G4, FR-14 / docx §8.4);
 * notification "hattiniz askiya alindi" SMS'i atar.
 */
public record SubscriptionSuspended(
        UUID eventId,
        UUID subscriptionId,
        UUID customerId,
        String msisdn,
        String reason) {
}
