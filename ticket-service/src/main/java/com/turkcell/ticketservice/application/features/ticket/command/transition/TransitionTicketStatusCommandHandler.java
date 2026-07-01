package com.turkcell.ticketservice.application.features.ticket.command.transition;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.ticketservice.application.features.ticket.mapper.TicketMapper;
import com.turkcell.ticketservice.application.features.ticket.rule.TicketBusinessRules;
import com.turkcell.ticketservice.dto.TicketResponse;
import com.turkcell.ticketservice.entity.Ticket;
import com.turkcell.ticketservice.repository.TicketRepository;

@Component
public class TransitionTicketStatusCommandHandler
        implements CommandHandler<TransitionTicketStatusCommand, TicketResponse> {

    private final TicketRepository repository;
    private final TicketBusinessRules rules;
    private final TicketMapper mapper;

    public TransitionTicketStatusCommandHandler(TicketRepository repository,
                                                TicketBusinessRules rules,
                                                TicketMapper mapper) {
        this.repository = repository;
        this.rules = rules;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TicketResponse handle(TransitionTicketStatusCommand command) {
        Ticket ticket = repository.findById(command.ticketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", command.ticketId().toString()));

        String target = command.targetStatus();
        rules.assertTransitionAllowed(ticket.getStatus(), target);

        ticket.setStatus(target);
        if ("RESOLVED".equals(target)) {
            ticket.setResolvedAt(Instant.now());
        } else if ("IN_PROGRESS".equals(target)) {
            ticket.setResolvedAt(null); // tekrar acilma
        }
        return mapper.toResponse(repository.save(ticket));
    }
}
