package com.turkcell.subscriptionservice.application.features.subscription.mapper;

import org.springframework.stereotype.Component;

import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.entity.Subscription;

/** Subscription entity -> response donusumu. */
@Component
public class SubscriptionMapper {

    public SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(
                s.getId(), s.getOrderId(), s.getCustomerId(), s.getMsisdn(),
                s.getTariffCode(), s.getStatus(), s.getActivatedAt(), s.getSuspendedAt(),
                s.getTerminatedAt());
    }
}
