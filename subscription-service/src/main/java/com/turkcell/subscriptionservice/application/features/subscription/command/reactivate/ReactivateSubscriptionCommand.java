package com.turkcell.subscriptionservice.application.features.subscription.command.reactivate;

import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;

public record ReactivateSubscriptionCommand(
        UUID subscriptionId,
        UUID actorUserId) implements Command<SubscriptionResponse> {
}
