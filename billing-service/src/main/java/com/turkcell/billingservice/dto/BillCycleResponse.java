package com.turkcell.billingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BillCycleResponse(
        UUID id,
        UUID customerId,
        UUID subscriptionId,
        BigDecimal monthlyFee,
        String currency,
        Short dayOfMonth,
        LocalDate nextRunDate) {
}
