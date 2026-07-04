package com.turkcell.subscriptionservice.application.features.subscription.command.reactivate;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.commonlib.saga.SagaTopics;
import com.turkcell.commonlib.saga.SubscriptionReactivated;
import com.turkcell.subscriptionservice.application.features.subscription.mapper.SubscriptionMapper;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.entity.Subscription;
import com.turkcell.subscriptionservice.exception.InvalidSubscriptionStateException;
import com.turkcell.subscriptionservice.repository.SubscriptionRepository;
import com.turkcell.subscriptionservice.saga.OutboxWriter;
import com.turkcell.subscriptionservice.service.AuditWriter;

/**
 * Abonelik yeniden aktivasyonu (G4, FR-14): yalniz SUSPENDED -> ACTIVE
 * (suspendedAt temizlenir). Durum + audit + SubscriptionReactivated outbox'i TEK transaction.
 */
@Component
public class ReactivateSubscriptionCommandHandler
        implements CommandHandler<ReactivateSubscriptionCommand, SubscriptionResponse> {

    private static final Logger log = LoggerFactory.getLogger(ReactivateSubscriptionCommandHandler.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper mapper;
    private final AuditWriter audit;
    private final OutboxWriter outbox;

    public ReactivateSubscriptionCommandHandler(SubscriptionRepository subscriptionRepository,
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
    public SubscriptionResponse handle(ReactivateSubscriptionCommand command) {
        Subscription sub = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", command.subscriptionId().toString()));

        if (!"SUSPENDED".equals(sub.getStatus())) {
            throw new InvalidSubscriptionStateException(
                    "yeniden aktivasyon yalniz SUSPENDED abonelikte yapilabilir (mevcut: " + sub.getStatus() + ")");
        }

        sub.setStatus("ACTIVE");
        sub.setSuspendedAt(null);
        Subscription saved = subscriptionRepository.save(sub);

        audit.write(command.actorUserId(), "Subscription", saved.getId(), "REACTIVATED",
                "msisdn=" + saved.getMsisdn());
        outbox.enqueue(SagaTopics.SUBSCRIPTION_EVENTS, "SubscriptionReactivated", saved.getId(),
                new SubscriptionReactivated(UUID.randomUUID(), saved.getId(), saved.getCustomerId(),
                        saved.getMsisdn()));

        log.info("subscription: yeniden aktive edildi. sub={} msisdn={} actor={}",
                saved.getId(), saved.getMsisdn(), command.actorUserId());
        return mapper.toResponse(saved);
    }
}
