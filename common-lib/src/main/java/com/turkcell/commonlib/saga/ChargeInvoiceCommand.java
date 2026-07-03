package com.turkcell.commonlib.saga;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Komut: billing -> payment ({@link SagaTopics#PAYMENT_COMMANDS}).
 * Aylik bill-run'da kesilen faturanin otomatik tahsilati (recurring auto-pay).
 * Reply: {@link InvoicePaid} / {@link InvoicePaymentFailed} ({@link SagaTopics#INVOICE_EVENTS}).
 */
public record ChargeInvoiceCommand(
        UUID eventId,
        UUID invoiceId,
        UUID customerId,
        BigDecimal amount,
        String currency) {
}
