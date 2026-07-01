package com.turkcell.usageservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** Kullanim/CDR kaydi girisi (ADMIN / teknik). recordedAt yoksa server 'now' atar. */
public record RecordUsageRequest(
        @NotNull UUID subscriptionId,
        @NotBlank @Pattern(regexp = "VOICE|SMS|DATA", message = "type: VOICE|SMS|DATA") String type,
        @NotNull @Positive BigDecimal quantity,
        Instant recordedAt,
        String cdrRef) {
}
