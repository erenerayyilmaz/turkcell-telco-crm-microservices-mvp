package com.turkcell.commonlib.saga;

import java.util.UUID;

/** Reply: payment -> billing ({@link SagaTopics#INVOICE_EVENTS}). Fatura tahsilati basarili -> invoice PAID. */
public record InvoicePaid(
        UUID eventId,
        UUID invoiceId,
        UUID paymentId) {
}
