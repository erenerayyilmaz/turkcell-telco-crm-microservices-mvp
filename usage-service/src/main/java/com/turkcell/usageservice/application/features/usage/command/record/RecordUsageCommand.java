package com.turkcell.usageservice.application.features.usage.command.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.usageservice.dto.UsageRecordResponse;

/** Tekil kullanim/CDR kaydi olusturma komutu (senkron REST girisi). */
public record RecordUsageCommand(
        UUID subscriptionId,
        String type,
        BigDecimal quantity,
        Instant recordedAt,
        String cdrRef) implements Command<UsageRecordResponse> {
}
