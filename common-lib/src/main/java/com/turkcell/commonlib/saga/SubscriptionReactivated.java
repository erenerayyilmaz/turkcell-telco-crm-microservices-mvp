package com.turkcell.commonlib.saga;

import java.util.UUID;

/**
 * Domain event: subscription -> dis dunya ({@link SagaTopics#SUBSCRIPTION_EVENTS}).
 * Abonelik SUSPENDED -> ACTIVE'e dondugunde yayinlanir (G4, FR-14);
 * notification "hattiniz yeniden acildi" SMS'i atar. (Docx §8.4 publish listesinde yok;
 * suspend/terminate ile simetri icin bilincli eklendi.)
 */
public record SubscriptionReactivated(
        UUID eventId,
        UUID subscriptionId,
        UUID customerId,
        String msisdn) {
}
