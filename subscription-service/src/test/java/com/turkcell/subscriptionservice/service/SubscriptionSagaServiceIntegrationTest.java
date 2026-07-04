package com.turkcell.subscriptionservice.service;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.turkcell.commonlib.saga.ActivateSubscriptionCommand;
import com.turkcell.commonlib.saga.ReleaseMsisdnCommand;
import com.turkcell.commonlib.saga.ReserveMsisdnCommand;
import com.turkcell.commonlib.saga.SagaTopics;
import com.turkcell.subscriptionservice.entity.Subscription;
import com.turkcell.subscriptionservice.repository.MsisdnPoolRepository;
import com.turkcell.subscriptionservice.repository.OutboxRepository;
import com.turkcell.subscriptionservice.repository.ProcessedEventRepository;
import com.turkcell.subscriptionservice.repository.SimCardRepository;
import com.turkcell.subscriptionservice.repository.SubscriptionRepository;
import com.turkcell.subscriptionservice.saga.OutboxWriter;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Saga participant inbox idempotency entegrasyon testi — GERCEK Postgres (Testcontainers).
 * V2 migration'i 20 FREE msisdn seed'ler; reserve -> activate -> release yasam dongusu
 * is tablolari (subscriptions/msisdn_pool/sim_cards) VE reply outbox'i ile birlikte dogrulanir.
 * OutboxWriter'in ihtiyac duydugu Jackson 3 ObjectMapper'i slice saglamadigi icin test config verir.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SubscriptionSagaService.class, OutboxWriter.class})
@Testcontainers(disabledWithoutDocker = true)
class SubscriptionSagaServiceIntegrationTest {

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
    SubscriptionSagaService service;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    MsisdnPoolRepository msisdnPoolRepository;

    @Autowired
    SimCardRepository simCardRepository;

    @Autowired
    OutboxRepository outboxRepository;

    @Autowired
    ProcessedEventRepository processedEventRepository;

    @Test
    @DisplayName("ayni ReserveMsisdnCommand eventId iki kez islenirse tek abonelik + tek RESERVED numara olusur")
    void sameReserveCommandTwiceReservesSingleMsisdn() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        ReserveMsisdnCommand cmd = new ReserveMsisdnCommand(eventId, orderId, UUID.randomUUID(), "TARIFE_M");

        service.reserve(cmd);
        service.reserve(cmd); // redelivery simulasyonu (at-least-once)

        Subscription sub = subscriptionRepository.findByOrderId(orderId).orElseThrow();
        assertThat(sub.getStatus()).isEqualTo("PENDING");
        assertThat(sub.getTariffCode()).isEqualTo("TARIFE_M");
        assertThat(subscriptionRepository.count()).as("tek abonelik yazilmali").isEqualTo(1);
        assertThat(countPool("RESERVED")).isEqualTo(1);
        assertThat(msisdnPoolRepository.findById(sub.getMsisdn()))
                .get()
                .satisfies(pool -> {
                    assertThat(pool.getStatus()).isEqualTo("RESERVED");
                    assertThat(pool.getReservedUntil()).isNotNull();
                });
        assertThat(countOutbox("MsisdnReserved", orderId)).isEqualTo(1);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
    }

    @Test
    @DisplayName("activate: abonelik ACTIVE olur, numara ALLOCATED'a gecer, SIM atanir; tekrar islenmez")
    void activateHappyPathAllocatesMsisdnAndSim() {
        UUID orderId = UUID.randomUUID();
        service.reserve(new ReserveMsisdnCommand(UUID.randomUUID(), orderId, UUID.randomUUID(), "TARIFE_M"));
        String msisdn = subscriptionRepository.findByOrderId(orderId).orElseThrow().getMsisdn();

        UUID eventId = UUID.randomUUID();
        ActivateSubscriptionCommand cmd = new ActivateSubscriptionCommand(eventId, orderId);
        service.activate(cmd);
        service.activate(cmd); // redelivery simulasyonu

        Subscription sub = subscriptionRepository.findByOrderId(orderId).orElseThrow();
        assertThat(sub.getStatus()).isEqualTo("ACTIVE");
        assertThat(sub.getActivatedAt()).isNotNull();
        assertThat(msisdnPoolRepository.findById(msisdn))
                .get()
                .satisfies(pool -> {
                    assertThat(pool.getStatus()).isEqualTo("ALLOCATED");
                    assertThat(pool.getReservedUntil()).isNull();
                });
        assertThat(simCardRepository.findAll())
                .as("tek SIM atanmali (idempotent)")
                .filteredOn(sim -> msisdn.equals(sim.getMsisdn()))
                .hasSize(1)
                .first()
                .satisfies(sim -> assertThat(sim.getStatus()).isEqualTo("ACTIVE"));
        assertThat(countOutbox("SubscriptionActivated", orderId)).isEqualTo(1);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
    }

    @Test
    @DisplayName("tarife _FAIL ile bitiyorsa aktivasyon reddedilir: SubscriptionActivationFailed reply, aktivasyon yok")
    void activateWithFailTariffEnqueuesActivationFailedReply() {
        UUID orderId = UUID.randomUUID();
        service.reserve(new ReserveMsisdnCommand(UUID.randomUUID(), orderId, UUID.randomUUID(), "TARIFE_X_FAIL"));
        String msisdn = subscriptionRepository.findByOrderId(orderId).orElseThrow().getMsisdn();

        service.activate(new ActivateSubscriptionCommand(UUID.randomUUID(), orderId));

        assertThat(countOutbox("SubscriptionActivationFailed", orderId)).isEqualTo(1);
        assertThat(countOutbox("SubscriptionActivated", orderId)).isZero();
        assertThat(subscriptionRepository.findByOrderId(orderId).orElseThrow().getStatus())
                .as("aktivasyon gerceklesmemeli")
                .isEqualTo("PENDING");
        assertThat(msisdnPoolRepository.findById(msisdn).orElseThrow().getStatus()).isEqualTo("RESERVED");
        assertThat(simCardRepository.findAll())
                .filteredOn(sim -> msisdn.equals(sim.getMsisdn()))
                .isEmpty();
    }

    @Test
    @DisplayName("release (compensation): abonelik CANCELLED, numara FREE'ye doner; tekrar islenmez")
    void releaseCancelsSubscriptionAndFreesMsisdn() {
        UUID orderId = UUID.randomUUID();
        service.reserve(new ReserveMsisdnCommand(UUID.randomUUID(), orderId, UUID.randomUUID(), "TARIFE_M"));
        String msisdn = subscriptionRepository.findByOrderId(orderId).orElseThrow().getMsisdn();

        UUID eventId = UUID.randomUUID();
        ReleaseMsisdnCommand cmd = new ReleaseMsisdnCommand(eventId, orderId, "payment failed");
        service.release(cmd);
        service.release(cmd); // redelivery simulasyonu

        Subscription sub = subscriptionRepository.findByOrderId(orderId).orElseThrow();
        assertThat(sub.getStatus()).isEqualTo("CANCELLED");
        assertThat(sub.getTerminatedAt()).isNotNull();
        assertThat(msisdnPoolRepository.findById(msisdn))
                .get()
                .satisfies(pool -> {
                    assertThat(pool.getStatus()).isEqualTo("FREE");
                    assertThat(pool.getReservedUntil()).isNull();
                });
        assertThat(countPool("RESERVED")).isZero();
        assertThat(countOutbox("MsisdnReleased", orderId)).isEqualTo(1);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
    }

    private long countPool(String status) {
        return msisdnPoolRepository.findAll().stream()
                .filter(pool -> status.equals(pool.getStatus()))
                .count();
    }

    private long countOutbox(String eventType, UUID orderId) {
        return outboxRepository.findAll().stream()
                .filter(e -> eventType.equals(e.getEventType()) && orderId.equals(e.getAggregateId()))
                .peek(e -> assertThat(e.getDestination()).isEqualTo(SagaTopics.SAGA_REPLIES))
                .count();
    }
}
