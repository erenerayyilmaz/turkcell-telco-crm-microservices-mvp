package com.turkcell.ticketservice.application.features.ticket.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.turkcell.ticketservice.application.features.ticket.command.create.CreateTicketCommand;
import com.turkcell.ticketservice.dto.TicketCommentResponse;
import com.turkcell.ticketservice.dto.TicketDetailResponse;
import com.turkcell.ticketservice.dto.TicketResponse;
import com.turkcell.ticketservice.entity.Ticket;
import com.turkcell.ticketservice.entity.TicketComment;

/** Ticket/TicketComment entity <-> command/response donusumleri. */
@Component
public class TicketMapper {

    /** Yeni OPEN talep olusturur (status/priority varsayilani entity'de). */
    public Ticket toNewTicket(CreateTicketCommand command) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(command.customerId());
        ticket.setCategory(command.category());
        if (command.priority() != null) {
            ticket.setPriority(command.priority());
        }
        ticket.setSlaDueAt(command.slaDueAt());
        return ticket;
    }

    public TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getCustomerId(),
                t.getCategory(),
                t.getPriority(),
                t.getStatus(),
                t.getAssignedTo(),
                t.getSlaDueAt(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getResolvedAt());
    }

    public TicketCommentResponse toCommentResponse(TicketComment c) {
        return new TicketCommentResponse(c.getId(), c.getAuthorId(), c.getBody(), c.getCreatedAt());
    }

    public TicketDetailResponse toDetail(Ticket ticket, List<TicketComment> comments) {
        return new TicketDetailResponse(
                toResponse(ticket),
                comments.stream().map(this::toCommentResponse).toList());
    }
}
