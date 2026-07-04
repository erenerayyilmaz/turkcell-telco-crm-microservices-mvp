package com.turkcell.commonlib.saga;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain event: billing -> dis dunya ({@link SagaTopics#INVOICE_EVENTS}).
 * Bill-run bir faturayi kestiginde (ISSUED) yayinlanir; notification
 * "faturaniz kesildi" e-postasi (mock) atar (docx senaryo 14.2, §8.8).
 * billing'in kendi consumer'i bu tipi atlar (yalniz tahsilat reply'larini isler).
 */
public record InvoiceGenerated(
        UUID eventId,
        UUID invoiceId,
        UUID customerId,
        UUID subscriptionId,
        BigDecimal grandTotal,
        String currency,
        LocalDate dueDate,
        LocalDate periodStart,
        LocalDate periodEnd) {
}
