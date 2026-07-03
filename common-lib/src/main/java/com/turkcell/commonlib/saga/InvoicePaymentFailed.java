package com.turkcell.commonlib.saga;

import java.util.UUID;

/** Reply: payment -> billing ({@link SagaTopics#INVOICE_EVENTS}). Tahsilat reddedildi -> invoice PAYMENT_FAILED (dunning konusu). */
public record InvoicePaymentFailed(
        UUID eventId,
        UUID invoiceId,
        String reason) {
}
