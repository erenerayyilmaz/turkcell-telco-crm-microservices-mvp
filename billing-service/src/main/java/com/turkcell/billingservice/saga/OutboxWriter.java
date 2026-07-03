package com.turkcell.billingservice.saga;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.turkcell.billingservice.entity.OutboxEvent;
import com.turkcell.billingservice.entity.OutboxStatus;
import com.turkcell.billingservice.repository.OutboxRepository;

import tools.jackson.databind.ObjectMapper;

/** Komut/event'leri transactional outbox'a yazar (fatura kesimi ile ayni transaction'da). */
@Component
public class OutboxWriter {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxWriter(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void enqueue(String destination, String eventType, UUID aggregateId, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setAggregateType("Invoice");
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setDestination(destination);
        event.setPayload(objectMapper.writeValueAsString(payload));
        event.setStatus(OutboxStatus.PENDING);
        event.setRetryCount(0);
        event.setCreatedAt(Instant.now());
        outboxRepository.save(event);
    }
}
