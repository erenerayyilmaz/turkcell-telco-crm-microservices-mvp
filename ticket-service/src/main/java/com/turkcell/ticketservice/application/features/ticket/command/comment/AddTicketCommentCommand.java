package com.turkcell.ticketservice.application.features.ticket.command.comment;

import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.ticketservice.dto.TicketCommentResponse;

/** Talebe yorum/not ekler (authorId = JWT 'sub' / UUID). */
public record AddTicketCommentCommand(
        UUID ticketId,
        UUID authorId,
        String body) implements Command<TicketCommentResponse> {
}
