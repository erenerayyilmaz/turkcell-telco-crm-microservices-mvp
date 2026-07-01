package com.turkcell.ticketservice.application.features.ticket.query.getbyid;

import java.util.UUID;

import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.ticketservice.dto.TicketDetailResponse;

/** Tekil talep + yorumlari (CSR/ADMIN). */
public record GetTicketByIdQuery(UUID id) implements Query<TicketDetailResponse> {
}
