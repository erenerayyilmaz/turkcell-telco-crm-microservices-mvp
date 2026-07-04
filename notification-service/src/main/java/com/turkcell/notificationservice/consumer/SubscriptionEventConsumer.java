package com.turkcell.notificationservice.consumer;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import com.turkcell.commonlib.saga.SagaHeaders;
import com.turkcell.commonlib.saga.SubscriptionReactivated;
import com.turkcell.commonlib.saga.SubscriptionSuspended;
import com.turkcell.commonlib.saga.SubscriptionTerminated;
import com.turkcell.notificationservice.service.SubscriptionEventHandler;

import tools.jackson.databind.ObjectMapper;

/**
 * subscription-events topic tuketicisi (G4, FR-14):
 * suspend/reactivate/terminate -> abonelik durumu SMS'leri.
 */
@Configuration
public class SubscriptionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventConsumer.class);

    private final SubscriptionEventHandler handler;
    private final ObjectMapper objectMapper;

    public SubscriptionEventConsumer(SubscriptionEventHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<byte[]>> consumeSubscriptionEvents() {
        return message -> {
            String type = eventType(message);
            switch (type) {
                case "SubscriptionSuspended" -> handler.handleSuspended(
                        objectMapper.readValue(message.getPayload(), SubscriptionSuspended.class));
                case "SubscriptionReactivated" -> handler.handleReactivated(
                        objectMapper.readValue(message.getPayload(), SubscriptionReactivated.class));
                case "SubscriptionTerminated" -> handler.handleTerminated(
                        objectMapper.readValue(message.getPayload(), SubscriptionTerminated.class));
                default -> log.debug("notification: ilgisiz subscription event atlandi: {}", type);
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
