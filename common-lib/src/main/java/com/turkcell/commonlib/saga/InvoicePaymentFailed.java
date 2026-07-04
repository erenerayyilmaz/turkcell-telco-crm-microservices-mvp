package com.turkcell.commonlib.saga;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Reply: payment -> billing ({@link SagaTopics#INVOICE_EVENTS}). Tahsilat reddedildi -> invoice PAYMENT_FAILED
 * (dunning konusu). customerId/amount/currency, notification'in ayni event'ten basarisiz odeme bildirimi
 * atabilmesi icin tasinir (event-carried state; payment bunlari ChargeInvoiceCommand'dan bilir).
 */
public record InvoicePaymentFailed(
        UUID eventId,
        UUID invoiceId,
        String reason,
        UUID customerId,
        BigDecimal amount,
        String currency) {
}
