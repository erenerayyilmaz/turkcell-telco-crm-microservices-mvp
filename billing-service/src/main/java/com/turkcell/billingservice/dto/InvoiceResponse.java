package com.turkcell.billingservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID customerId,
        UUID subscriptionId,
        UUID billCycleId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal subTotal,
        BigDecimal tax,
        BigDecimal grandTotal,
        String status,
        LocalDate dueDate,
        Instant issuedAt) {
}
