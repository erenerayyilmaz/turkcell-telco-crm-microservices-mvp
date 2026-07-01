package com.turkcell.ticketservice.application.features.ticket.query.list;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.ticketservice.dto.TicketResponse;

/** Sayfali talep listesi; status ve/veya customerId ile opsiyonel filtre (CSR/ADMIN). */
public record ListTicketsQuery(
        Pageable pageable,
        String status,
        UUID customerId) implements Query<RestPage<TicketResponse>> {
}
