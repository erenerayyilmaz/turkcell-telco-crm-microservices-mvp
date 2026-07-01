package com.turkcell.ticketservice.dto;

import java.util.List;

/** Tekil talep gorunumu: talep + kronolojik yorumlar. */
public record TicketDetailResponse(
        TicketResponse ticket,
        List<TicketCommentResponse> comments) {
}
