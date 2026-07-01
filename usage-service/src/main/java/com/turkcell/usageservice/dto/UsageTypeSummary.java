package com.turkcell.usageservice.dto;

import java.math.BigDecimal;

/** Bir kullanim tipi icin donem toplami. */
public record UsageTypeSummary(
        String type,
        BigDecimal totalQuantity,
        long recordCount) {
}
