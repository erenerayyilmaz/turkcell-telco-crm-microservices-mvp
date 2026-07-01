package com.turkcell.ticketservice.application.features.ticket.command.create;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.ticketservice.application.features.ticket.mapper.TicketMapper;
import com.turkcell.ticketservice.dto.TicketResponse;
import com.turkcell.ticketservice.entity.Ticket;
import com.turkcell.ticketservice.repository.TicketRepository;

@Component
public class CreateTicketCommandHandler implements CommandHandler<CreateTicketCommand, TicketResponse> {

    private final TicketRepository repository;
    private final TicketMapper mapper;

    public CreateTicketCommandHandler(TicketRepository repository, TicketMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TicketResponse handle(CreateTicketCommand command) {
        Ticket saved = repository.save(mapper.toNewTicket(command));
        return mapper.toResponse(saved);
    }
}
