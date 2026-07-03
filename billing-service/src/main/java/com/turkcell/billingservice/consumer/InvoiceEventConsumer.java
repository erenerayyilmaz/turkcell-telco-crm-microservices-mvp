package com.turkcell.billingservice.consumer;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import com.turkcell.billingservice.service.InvoiceEventHandler;
import com.turkcell.commonlib.saga.InvoicePaid;
import com.turkcell.commonlib.saga.InvoicePaymentFailed;
import com.turkcell.commonlib.saga.SagaHeaders;

import tools.jackson.databind.ObjectMapper;

/**
 * invoice-events topic tuketicisi: payment'in fatura tahsilat reply'lari.
 * InvoicePaid -> invoice PAID; InvoicePaymentFailed -> invoice PAYMENT_FAILED.
 */
@Configuration
public class InvoiceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventConsumer.class);

    private final InvoiceEventHandler handler;
    private final ObjectMapper objectMapper;

    public InvoiceEventConsumer(InvoiceEventHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<byte[]>> consumeInvoiceEvents() {
        return message -> {
            String type = eventType(message);
            byte[] body = message.getPayload();
            switch (type) {
                case "InvoicePaid" -> handler.onPaid(objectMapper.readValue(body, InvoicePaid.class));
                case "InvoicePaymentFailed" ->
                        handler.onPaymentFailed(objectMapper.readValue(body, InvoicePaymentFailed.class));
                default -> log.debug("billing: ilgisiz invoice event atlandi: {}", type);
            }
        };
    }

    private static String eventType(Message<byte[]> message) {
        Object raw = message.getHeaders().get(SagaHeaders.EVENT_TYPE);
        if (raw instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return String.valueOf(raw);
    }
}
