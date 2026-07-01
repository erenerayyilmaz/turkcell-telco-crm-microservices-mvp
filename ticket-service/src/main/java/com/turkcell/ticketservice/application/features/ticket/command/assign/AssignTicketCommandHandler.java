package com.turkcell.ticketservice.application.features.ticket.command.assign;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.ticketservice.application.features.ticket.mapper.TicketMapper;
import com.turkcell.ticketservice.dto.TicketResponse;
import com.turkcell.ticketservice.entity.Ticket;
import com.turkcell.ticketservice.repository.TicketRepository;

@Component
public class AssignTicketCommandHandler implements CommandHandler<AssignTicketCommand, TicketResponse> {

    private final TicketRepository repository;
    private final TicketMapper mapper;

    public AssignTicketCommandHandler(TicketRepository repository, TicketMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TicketResponse handle(AssignTicketCommand command) {
        Ticket ticket = repository.findById(command.ticketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", command.ticketId().toString()));
        ticket.setAssignedTo(command.assigneeId());
        return mapper.toResponse(repository.save(ticket));
    }
}
