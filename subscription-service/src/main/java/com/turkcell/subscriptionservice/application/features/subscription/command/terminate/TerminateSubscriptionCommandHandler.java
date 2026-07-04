package com.turkcell.subscriptionservice.application.features.subscription.command.terminate;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.commonlib.saga.SagaTopics;
import com.turkcell.commonlib.saga.SubscriptionTerminated;
import com.turkcell.subscriptionservice.application.features.subscription.mapper.SubscriptionMapper;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.entity.Subscription;
import com.turkcell.subscriptionservice.exception.InvalidSubscriptionStateException;
import com.turkcell.subscriptionservice.repository.MsisdnPoolRepository;
import com.turkcell.subscriptionservice.repository.SimCardRepository;
import com.turkcell.subscriptionservice.repository.SubscriptionRepository;
import com.turkcell.subscriptionservice.saga.OutboxWriter;
import com.turkcell.subscriptionservice.service.AuditWriter;

/**
 * Abonelik sonlandirma (G4, FR-14): ACTIVE veya SUSPENDED -> TERMINATED (terminal durum).
 * MSISDN havuza doner (FREE, §8.4 "MSISDN allocation/release"), aktif SIM'ler DEACTIVATED
 * olur. Durum + audit + SubscriptionTerminated outbox'i TEK transaction.
 */
@Component
public class TerminateSubscriptionCommandHandler
        implements CommandHandler<TerminateSubscriptionCommand, SubscriptionResponse> {

    private static final Logger log = LoggerFactory.getLogger(TerminateSubscriptionCommandHandler.class);
    private static final Set<String> TERMINATABLE = Set.of("ACTIVE", "SUSPENDED");

    private final SubscriptionRepository subscriptionRepository;
    private final MsisdnPoolRepository msisdnPoolRepository;
    private final SimCardRepository simCardRepository;
    private final SubscriptionMapper mapper;
    private final AuditWriter audit;
    private final OutboxWriter outbox;

    public TerminateSubscriptionCommandHandler(SubscriptionRepository subscriptionRepository,
                                               MsisdnPoolRepository msisdnPoolRepository,
                                               SimCardRepository simCardRepository,
                                               SubscriptionMapper mapper,
                                               AuditWriter audit,
                                               OutboxWriter outbox) {
        this.subscriptionRepository = subscriptionRepository;
        this.msisdnPoolRepository = msisdnPoolRepository;
        this.simCardRepository = simCardRepository;
        this.mapper = mapper;
        this.audit = audit;
        this.outbox = outbox;
    }

    @Override
    @Transactional
    public SubscriptionResponse handle(TerminateSubscriptionCommand command) {
        Subscription sub = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", command.subscriptionId().toString()));

        if (!TERMINATABLE.contains(sub.getStatus())) {
            throw new InvalidSubscriptionStateException(
                    "sonlandirma yalniz ACTIVE/SUSPENDED abonelikte yapilabilir (mevcut: " + sub.getStatus() + ")");
        }

        sub.setStatus("TERMINATED");
        sub.setTerminatedAt(Instant.now());
        Subscription saved = subscriptionRepository.save(sub);

        msisdnPoolRepository.findById(saved.getMsisdn()).ifPresent(pool -> {
            pool.setStatus("FREE");
            pool.setReservedUntil(null);
            msisdnPoolRepository.save(pool);
        });
        simCardRepository.findByMsisdn(saved.getMsisdn()).stream()
                .filter(sim -> "ACTIVE".equals(sim.getStatus()))
                .forEach(sim -> {
                    sim.setStatus("DEACTIVATED");
                    simCardRepository.save(sim);
                });

        audit.write(command.actorUserId(), "Subscription", saved.getId(), "TERMINATED",
                "msisdn=" + saved.getMsisdn() + " reason=" + command.reason());
        outbox.enqueue(SagaTopics.SUBSCRIPTION_EVENTS, "SubscriptionTerminated", saved.getId(),
                new SubscriptionTerminated(UUID.randomUUID(), saved.getId(), saved.getCustomerId(),
                        saved.getMsisdn(), command.reason()));

        log.info("subscription: sonlandirildi. sub={} msisdn={} actor={}",
                saved.getId(), saved.getMsisdn(), command.actorUserId());
        return mapper.toResponse(saved);
    }
}
