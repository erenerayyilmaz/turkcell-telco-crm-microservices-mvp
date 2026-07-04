package com.turkcell.notificationservice.consumer;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import com.turkcell.commonlib.saga.InvoiceGenerated;
import com.turkcell.commonlib.saga.InvoicePaid;
import com.turkcell.commonlib.saga.InvoicePaymentFailed;
import com.turkcell.commonlib.saga.SagaHeaders;
import com.turkcell.notificationservice.service.InvoiceEventHandler;

import tools.jackson.databind.ObjectMapper;

/**
 * invoice-events topic tuketicisi (notification tarafi): fatura yasam dongusu
 * bildirimleri. Ayni topic'i billing kendi group'uyla tuketir (tahsilat reply'lari);
 * burada uc event de bildirime cevrilir.
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
                case "InvoiceGenerated" -> handler.handleGenerated(objectMapper.readValue(body, InvoiceGenerated.class));
                case "InvoicePaid" -> handler.handlePaid(objectMapper.readValue(body, InvoicePaid.class));
                case "InvoicePaymentFailed" ->
                        handler.handlePaymentFailed(objectMapper.readValue(body, InvoicePaymentFailed.class));
                default -> log.debug("notification: ilgisiz invoice event atlandi: {}", type);
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
