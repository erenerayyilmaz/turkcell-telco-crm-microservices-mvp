package com.turkcell.usageservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UsageRecordResponse(
        UUID id,
        UUID subscriptionId,
        String type,
        BigDecimal quantity,
        Instant recordedAt,
        String cdrRef) {
}
