package com.turkcell.subscriptionservice.application.features.subscription.command.suspend;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.commonlib.saga.SagaTopics;
import com.turkcell.commonlib.saga.SubscriptionSuspended;
import com.turkcell.subscriptionservice.application.features.subscription.mapper.SubscriptionMapper;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.entity.Subscription;
import com.turkcell.subscriptionservice.exception.InvalidSubscriptionStateException;
import com.turkcell.subscriptionservice.repository.SubscriptionRepository;
import com.turkcell.subscriptionservice.saga.OutboxWriter;
import com.turkcell.subscriptionservice.service.AuditWriter;

/**
 * Abonelik askiya alma (G4, FR-14): yalniz ACTIVE -> SUSPENDED.
 * Durum degisikligi + audit + SubscriptionSuspended outbox'i TEK transaction.
 */
@Component
public class SuspendSubscriptionCommandHandler
        implements CommandHandler<SuspendSubscriptionCommand, SubscriptionResponse> {

    private static final Logger log = LoggerFactory.getLogger(SuspendSubscriptionCommandHandler.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper mapper;
    private final AuditWriter audit;
    private final OutboxWriter outbox;

    public SuspendSubscriptionCommandHandler(SubscriptionRepository subscriptionRepository,
                                             SubscriptionMapper mapper,
                                             AuditWriter audit,
                                             OutboxWriter outbox) {
        this.subscriptionRepository = subscriptionRepository;
        this.mapper = mapper;
        this.audit = audit;
        this.outbox = outbox;
    }

    @Override
    @Transactional
    public SubscriptionResponse handle(SuspendSubscriptionCommand command) {
        Subscription sub = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", command.subscriptionId().toString()));

        if (!"ACTIVE".equals(sub.getStatus())) {
            throw new InvalidSubscriptionStateException(
                    "askiya alma yalniz ACTIVE abonelikte yapilabilir (mevcut: " + sub.getStatus() + ")");
        }

        sub.setStatus("SUSPENDED");
        sub.setSuspendedAt(Instant.now());
        Subscription saved = subscriptionRepository.save(sub);

        audit.write(command.actorUserId(), "Subscription", saved.getId(), "SUSPENDED",
                "msisdn=" + saved.getMsisdn() + " reason=" + command.reason());
        outbox.enqueue(SagaTopics.SUBSCRIPTION_EVENTS, "SubscriptionSuspended", saved.getId(),
                new SubscriptionSuspended(UUID.randomUUID(), saved.getId(), saved.getCustomerId(),
                        saved.getMsisdn(), command.reason()));

        log.info("subscription: askiya alindi. sub={} msisdn={} actor={}",
                saved.getId(), saved.getMsisdn(), command.actorUserId());
        return mapper.toResponse(saved);
    }
}
