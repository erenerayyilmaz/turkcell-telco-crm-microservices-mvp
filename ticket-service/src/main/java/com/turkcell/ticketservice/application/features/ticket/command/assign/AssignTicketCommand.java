package com.turkcell.ticketservice.application.features.ticket.command.assign;

import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.ticketservice.dto.TicketResponse;

/** Talebi bir CSR'e atar (assigneeId = Keycloak sub / UUID). */
public record AssignTicketCommand(
        UUID ticketId,
        UUID assigneeId) implements Command<TicketResponse> {
}
