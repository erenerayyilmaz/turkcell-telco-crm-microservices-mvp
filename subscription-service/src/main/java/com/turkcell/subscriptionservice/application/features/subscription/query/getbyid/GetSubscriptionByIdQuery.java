package com.turkcell.subscriptionservice.application.features.subscription.query.getbyid;

import java.util.UUID;

import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;

/** Tekil abonelik sorgusu (CSR/ADMIN). */
public record GetSubscriptionByIdQuery(UUID id) implements Query<SubscriptionResponse> {
}
