package com.turkcell.subscriptionservice.application.features.subscription.command.suspend;

import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;

public record SuspendSubscriptionCommand(
        UUID subscriptionId,
        UUID actorUserId,
        String reason) implements Command<SubscriptionResponse> {
}
