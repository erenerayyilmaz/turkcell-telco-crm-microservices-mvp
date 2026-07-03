package com.turkcell.subscriptionservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.subscriptionservice.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByOrderId(UUID orderId);

    Page<Subscription> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Subscription> findByStatus(String status, Pageable pageable);

    Page<Subscription> findByCustomerIdAndStatus(UUID customerId, String status, Pageable pageable);
}
