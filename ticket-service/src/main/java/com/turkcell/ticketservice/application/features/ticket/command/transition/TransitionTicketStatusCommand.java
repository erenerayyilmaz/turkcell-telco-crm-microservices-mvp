package com.turkcell.ticketservice.application.features.ticket.command.transition;

import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.ticketservice.dto.TicketResponse;

/** Talebi durum makinesinde bir sonraki gecerli duruma tasir. */
public record TransitionTicketStatusCommand(
        UUID ticketId,
        String targetStatus) implements Command<TicketResponse> {
}
