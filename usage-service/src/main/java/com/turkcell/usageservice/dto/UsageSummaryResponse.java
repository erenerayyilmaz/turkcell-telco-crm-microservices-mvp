package com.turkcell.usageservice.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Abonelik bazli donem kullanim ozeti (billing icin veri kaynagi). */
public record UsageSummaryResponse(
        UUID subscriptionId,
        Instant from,
        Instant to,
        List<UsageTypeSummary> byType,
        long totalRecords) {
}
