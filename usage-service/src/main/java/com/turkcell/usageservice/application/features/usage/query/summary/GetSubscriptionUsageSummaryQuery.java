package com.turkcell.usageservice.application.features.usage.query.summary;

import java.time.Instant;
import java.util.UUID;

import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.usageservice.dto.UsageSummaryResponse;

/** Bir abonelik icin [from, to) araligindaki tip bazli kullanim ozeti. */
public record GetSubscriptionUsageSummaryQuery(
        UUID subscriptionId,
        Instant from,
        Instant to) implements Query<UsageSummaryResponse> {
}
