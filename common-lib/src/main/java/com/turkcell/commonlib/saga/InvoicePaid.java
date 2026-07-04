package com.turkcell.commonlib.saga;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Reply: payment -> billing ({@link SagaTopics#INVOICE_EVENTS}). Fatura tahsilati basarili -> invoice PAID.
 * customerId/amount/currency, notification'in ayni event'ten odeme bildirimi atabilmesi icin tasinir
 * (event-carried state; payment bunlari ChargeInvoiceCommand'dan bilir).
 */
public record InvoicePaid(
        UUID eventId,
        UUID invoiceId,
        UUID paymentId,
        UUID customerId,
        BigDecimal amount,
        String currency) {
}
