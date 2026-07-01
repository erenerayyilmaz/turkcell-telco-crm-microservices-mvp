package com.turkcell.usageservice.consumer;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import com.turkcell.commonlib.saga.SagaHeaders;
import com.turkcell.usageservice.event.UsageRecordedEvent;
import com.turkcell.usageservice.service.UsageEventHandler;

import tools.jackson.databind.ObjectMapper;

/**
 * usage-events topic tuketicisi. Payload ham JSON byte; {@code eventType} header'ina gore dispatch.
 * "UsageRecorded" event'i inbox idempotency ile usage_records'a yazilir.
 */
@Configuration
public class UsageEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UsageEventConsumer.class);

    private final UsageEventHandler handler;
    private final ObjectMapper objectMapper;

    public UsageEventConsumer(UsageEventHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<byte[]>> consumeUsageEvents() {
        return message -> {
            String type = eventType(message);
            if ("UsageRecorded".equals(type)) {
                handler.handle(objectMapper.readValue(message.getPayload(), UsageRecordedEvent.class));
            } else {
                log.debug("usage: ilgisiz event atlandi: {}", type);
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
