package com.turkcell.usageservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * usage-events topic'inden tuketilen CDR/kullanim event'i (yerel kontrat; henuz
 * platformda producer'i yok, ileride mediation/CDR sistemi yayinlayacak).
 * {@code eventId} inbox idempotency anahtaridir.
 */
public record UsageRecordedEvent(
        UUID eventId,
        UUID subscriptionId,
        String type,
        BigDecimal quantity,
        Instant recordedAt,
        String cdrRef) {
}
