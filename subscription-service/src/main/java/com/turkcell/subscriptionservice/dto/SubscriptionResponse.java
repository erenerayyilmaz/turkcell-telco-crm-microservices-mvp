package com.turkcell.subscriptionservice.dto;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        String msisdn,
        String tariffCode,
        String status,
        Instant activatedAt,
        Instant terminatedAt) {
}
