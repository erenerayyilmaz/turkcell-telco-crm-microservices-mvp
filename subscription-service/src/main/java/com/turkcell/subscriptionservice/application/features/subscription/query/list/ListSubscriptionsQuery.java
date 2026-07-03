package com.turkcell.subscriptionservice.application.features.subscription.query.list;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;

/** Sayfali abonelik listesi; customerId ve/veya status ile opsiyonel filtre (CSR/ADMIN). */
public record ListSubscriptionsQuery(
        Pageable pageable,
        UUID customerId,
        String status) implements Query<RestPage<SubscriptionResponse>> {
}
