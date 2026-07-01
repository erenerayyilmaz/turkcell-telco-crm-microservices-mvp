package com.turkcell.ticketservice.application.features.ticket.query.getbyid;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.ticketservice.application.features.ticket.mapper.TicketMapper;
import com.turkcell.ticketservice.dto.TicketDetailResponse;
import com.turkcell.ticketservice.entity.Ticket;
import com.turkcell.ticketservice.entity.TicketComment;
import com.turkcell.ticketservice.repository.TicketCommentRepository;
import com.turkcell.ticketservice.repository.TicketRepository;

@Component
public class GetTicketByIdQueryHandler implements QueryHandler<GetTicketByIdQuery, TicketDetailResponse> {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketMapper mapper;

    public GetTicketByIdQueryHandler(TicketRepository ticketRepository,
                                     TicketCommentRepository commentRepository,
                                     TicketMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse handle(GetTicketByIdQuery query) {
        Ticket ticket = ticketRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", query.id().toString()));
        List<TicketComment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());
        return mapper.toDetail(ticket, comments);
    }
}
