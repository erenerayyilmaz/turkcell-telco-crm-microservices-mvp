package com.turkcell.ticketservice.application.features.ticket.command.comment;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.ticketservice.application.features.ticket.mapper.TicketMapper;
import com.turkcell.ticketservice.dto.TicketCommentResponse;
import com.turkcell.ticketservice.entity.TicketComment;
import com.turkcell.ticketservice.repository.TicketCommentRepository;
import com.turkcell.ticketservice.repository.TicketRepository;

@Component
public class AddTicketCommentCommandHandler
        implements CommandHandler<AddTicketCommentCommand, TicketCommentResponse> {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketMapper mapper;

    public AddTicketCommentCommandHandler(TicketRepository ticketRepository,
                                          TicketCommentRepository commentRepository,
                                          TicketMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TicketCommentResponse handle(AddTicketCommentCommand command) {
        if (!ticketRepository.existsById(command.ticketId())) {
            throw new ResourceNotFoundException("Ticket", command.ticketId().toString());
        }
        TicketComment comment = new TicketComment();
        comment.setTicketId(command.ticketId());
        comment.setAuthorId(command.authorId());
        comment.setBody(command.body());
        return mapper.toCommentResponse(commentRepository.save(comment));
    }
}
