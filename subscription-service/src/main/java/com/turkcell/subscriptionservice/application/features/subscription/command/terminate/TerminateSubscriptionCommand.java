package com.turkcell.subscriptionservice.application.features.subscription.command.terminate;

import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;

public record TerminateSubscriptionCommand(
        UUID subscriptionId,
        UUID actorUserId,
        String reason) implements Command<SubscriptionResponse> {
}
