package com.turkcell.ticketservice.dto;

import java.time.Instant;
import java.util.UUID;

public record TicketCommentResponse(
        UUID id,
        UUID authorId,
        String body,
        Instant createdAt) {
}
