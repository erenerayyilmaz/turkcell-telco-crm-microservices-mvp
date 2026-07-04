package com.turkcell.subscriptionservice.application.features.subscription.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.commonlib.saga.SagaTopics;
import com.turkcell.subscriptionservice.application.features.subscription.command.reactivate.ReactivateSubscriptionCommand;
import com.turkcell.subscriptionservice.application.features.subscription.command.reactivate.ReactivateSubscriptionCommandHandler;
import com.turkcell.subscriptionservice.application.features.subscription.command.suspend.SuspendSubscriptionCommand;
import com.turkcell.subscriptionservice.application.features.subscription.command.suspend.SuspendSubscriptionCommandHandler;
import com.turkcell.subscriptionservice.application.features.subscription.command.terminate.TerminateSubscriptionCommand;
import com.turkcell.subscriptionservice.application.features.subscription.command.terminate.TerminateSubscriptionCommandHandler;
import com.turkcell.subscriptionservice.application.features.subscription.mapper.SubscriptionMapper;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.entity.SimCard;
import com.turkcell.subscriptionservice.entity.Subscription;
import com.turkcell.subscriptionservice.exception.InvalidSubscriptionStateException;
import com.turkcell.subscriptionservice.repository.AuditLogRepository;
import com.turkcell.subscriptionservice.repository.MsisdnPoolRepository;
import com.turkcell.subscriptionservice.repository.OutboxRepository;
import com.turkcell.subscriptionservice.repository.SimCardRepository;
import com.turkcell.subscriptionservice.repository.SubscriptionRepository;
import com.turkcell.subscriptionservice.saga.OutboxWriter;
import com.turkcell.subscriptionservice.service.AuditWriter;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Abonelik yasam dongusu entegrasyon testi (G4, FR-14) — GERCEK Postgres (Testcontainers).
 * Durum makinesi (ACTIVE -> SUSPENDED -> ACTIVE, ACTIVE/SUSPENDED -> TERMINATED),
 * gecersiz gecislerin 409'u, audit (actorUserId dahil) ve subscription-events outbox'i
 * birlikte dogrulanir. V2 migration'i 20 FREE msisdn seed'ler; testler bunlardan birini
 * ALLOCATED'a cekip ACTIVE abonelik kurar.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SuspendSubscriptionCommandHandler.class, ReactivateSubscriptionCommandHandler.class,
        TerminateSubscriptionCommandHandler.class, SubscriptionMapper.class,
        AuditWriter.class, OutboxWriter.class})
@Testcontainers(disabledWithoutDocker = true)
class SubscriptionLifecycleIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @TestConfiguration(proxyBeanMethods = false)
    static class JacksonTestConfig {
        @Bean
        ObjectMapper testObjectMapper() {
            return JsonMapper.builder().build();
        }
    }

    @Autowired
    SuspendSubscriptionCommandHandler suspendHandler;

    @Autowired
    ReactivateSubscriptionCommandHandler reactivateHandler;

    @Autowired
    TerminateSubscriptionCommandHandler terminateHandler;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    MsisdnPoolRepository msisdnPoolRepository;

    @Autowired
    SimCardRepository simCardRepository;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    OutboxRepository outboxRepository;

    static final UUID ACTOR = UUID.randomUUID();

    @Test
    @DisplayName("suspend: ACTIVE -> SUSPENDED; suspendedAt dolar, audit actor'lu yazilir, SubscriptionSuspended outbox'a girer")
    void suspendMovesActiveToSuspended() {
        Subscription sub = activeSubscription();

        SubscriptionResponse response = suspendHandler.handle(
                new SuspendSubscriptionCommand(sub.getId(), ACTOR, "odeme gecikmesi"));

        assertThat(response.status()).isEqualTo("SUSPENDED");
        assertThat(response.suspendedAt()).isNotNull();
        Subscription saved = subscriptionRepository.findById(sub.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo("SUSPENDED");
        assertThat(saved.getSuspendedAt()).isNotNull();
        assertThat(msisdnPoolRepository.findById(sub.getMsisdn()).orElseThrow().getStatus())
                .as("askida numara havuza donmez")
                .isEqualTo("ALLOCATED");
        assertAudit(sub.getId(), "SUSPENDED");
        assertThat(countOutbox("SubscriptionSuspended", sub.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("suspend: ACTIVE olmayan abonelikte 409 InvalidSubscriptionStateException, outbox yazilmaz")
    void suspendNonActiveRejected() {
        Subscription sub = activeSubscription();
        suspendHandler.handle(new SuspendSubscriptionCommand(sub.getId(), ACTOR, null));

        assertThatThrownBy(() -> suspendHandler.handle(new SuspendSubscriptionCommand(sub.getId(), ACTOR, null)))
                .isInstanceOf(InvalidSubscriptionStateException.class)
                .hasMessageContaining("SUSPENDED");
        assertThat(countOutbox("SubscriptionSuspended", sub.getId()))
                .as("ikinci suspend event uretmemeli")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("reactivate: SUSPENDED -> ACTIVE; suspendedAt temizlenir, SubscriptionReactivated outbox'a girer")
    void reactivateMovesSuspendedBackToActive() {
        Subscription sub = activeSubscription();
        suspendHandler.handle(new SuspendSubscriptionCommand(sub.getId(), ACTOR, null));

        SubscriptionResponse response = reactivateHandler.handle(
                new ReactivateSubscriptionCommand(sub.getId(), ACTOR));

        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.suspendedAt()).isNull();
        Subscription saved = subscriptionRepository.findById(sub.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getSuspendedAt()).isNull();
        assertAudit(sub.getId(), "REACTIVATED");
        assertThat(countOutbox("SubscriptionReactivated", sub.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("reactivate: SUSPENDED olmayan abonelikte 409")
    void reactivateNonSuspendedRejected() {
        Subscription sub = activeSubscription();

        assertThatThrownBy(() -> reactivateHandler.handle(new ReactivateSubscriptionCommand(sub.getId(), ACTOR)))
                .isInstanceOf(InvalidSubscriptionStateException.class)
                .hasMessageContaining("ACTIVE");
        assertThat(countOutbox("SubscriptionReactivated", sub.getId())).isZero();
    }

    @Test
    @DisplayName("terminate (ACTIVE'den): TERMINATED + terminatedAt; numara FREE'ye doner, SIM DEACTIVATED, event outbox'a girer")
    void terminateFromActiveReleasesMsisdnAndSim() {
        Subscription sub = activeSubscription();

        SubscriptionResponse response = terminateHandler.handle(
                new TerminateSubscriptionCommand(sub.getId(), ACTOR, "musteri istegi"));

        assertThat(response.status()).isEqualTo("TERMINATED");
        assertThat(response.terminatedAt()).isNotNull();
        assertThat(msisdnPoolRepository.findById(sub.getMsisdn()))
                .get()
                .satisfies(pool -> {
                    assertThat(pool.getStatus()).isEqualTo("FREE");
                    assertThat(pool.getReservedUntil()).isNull();
                });
        assertThat(simCardRepository.findByMsisdn(sub.getMsisdn()))
                .allSatisfy(sim -> assertThat(sim.getStatus()).isEqualTo("DEACTIVATED"));
        assertAudit(sub.getId(), "TERMINATED");
        assertThat(countOutbox("SubscriptionTerminated", sub.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("terminate (SUSPENDED'dan): askidaki abonelik de sonlandirilabilir")
    void terminateFromSuspendedAllowed() {
        Subscription sub = activeSubscription();
        suspendHandler.handle(new SuspendSubscriptionCommand(sub.getId(), ACTOR, null));

        SubscriptionResponse response = terminateHandler.handle(
                new TerminateSubscriptionCommand(sub.getId(), ACTOR, null));

        assertThat(response.status()).isEqualTo("TERMINATED");
        assertThat(countOutbox("SubscriptionTerminated", sub.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("terminate: TERMINATED abonelikte tekrar sonlandirma 409 (terminal durum)")
    void terminateIsTerminal() {
        Subscription sub = activeSubscription();
        terminateHandler.handle(new TerminateSubscriptionCommand(sub.getId(), ACTOR, null));

        assertThatThrownBy(() -> terminateHandler.handle(new TerminateSubscriptionCommand(sub.getId(), ACTOR, null)))
                .isInstanceOf(InvalidSubscriptionStateException.class)
                .hasMessageContaining("TERMINATED");
        assertThat(countOutbox("SubscriptionTerminated", sub.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("bilinmeyen abonelik id'sinde 404 ResourceNotFoundException")
    void unknownSubscriptionNotFound() {
        UUID unknown = UUID.randomUUID();
        assertThatThrownBy(() -> suspendHandler.handle(new SuspendSubscriptionCommand(unknown, ACTOR, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /** Seed havuzdan bir FREE numarayi ALLOCATED'a ceker, ACTIVE abonelik + ACTIVE SIM kurar. */
    private Subscription activeSubscription() {
        var pool = msisdnPoolRepository.findFirstByStatusOrderByMsisdn("FREE").orElseThrow();
        pool.setStatus("ALLOCATED");
        msisdnPoolRepository.save(pool);

        SimCard sim = new SimCard();
        sim.setIccid("899000000000000000" + (System.nanoTime() % 10_000));
        sim.setImsi("28601" + String.format("%010d", System.nanoTime() % 10_000_000_000L));
        sim.setMsisdn(pool.getMsisdn());
        sim.setStatus("ACTIVE");
        simCardRepository.save(sim);

        Subscription sub = new Subscription();
        sub.setOrderId(UUID.randomUUID());
        sub.setCustomerId(UUID.randomUUID());
        sub.setMsisdn(pool.getMsisdn());
        sub.setTariffCode("TARIFE_M");
        sub.setStatus("ACTIVE");
        sub.setActivatedAt(Instant.now());
        return subscriptionRepository.save(sub);
    }

    private void assertAudit(UUID subscriptionId, String action) {
        assertThat(auditLogRepository.findAll())
                .filteredOn(a -> subscriptionId.equals(a.getEntityId()) && action.equals(a.getAction()))
                .hasSize(1)
                .first()
                .satisfies(a -> {
                    assertThat(a.getActorUserId()).isEqualTo(ACTOR);
                    assertThat(a.getServiceName()).isEqualTo("subscription-service");
                    assertThat(a.getEntityName()).isEqualTo("Subscription");
                });
    }

    private long countOutbox(String eventType, UUID subscriptionId) {
        return outboxRepository.findAll().stream()
                .filter(e -> eventType.equals(e.getEventType()) && subscriptionId.equals(e.getAggregateId()))
                .peek(e -> assertThat(e.getDestination()).isEqualTo(SagaTopics.SUBSCRIPTION_EVENTS))
                .count();
    }
}
