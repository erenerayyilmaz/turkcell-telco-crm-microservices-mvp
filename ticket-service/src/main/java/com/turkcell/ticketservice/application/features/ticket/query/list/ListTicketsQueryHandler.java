package com.turkcell.ticketservice.application.features.ticket.query.list;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.ticketservice.application.features.ticket.mapper.TicketMapper;
import com.turkcell.ticketservice.dto.TicketResponse;
import com.turkcell.ticketservice.entity.Ticket;
import com.turkcell.ticketservice.repository.TicketRepository;

@Component
public class ListTicketsQueryHandler implements QueryHandler<ListTicketsQuery, RestPage<TicketResponse>> {

    private final TicketRepository repository;
    private final TicketMapper mapper;

    public ListTicketsQueryHandler(TicketRepository repository, TicketMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RestPage<TicketResponse> handle(ListTicketsQuery query) {
        boolean byCustomer = query.customerId() != null;
        boolean byStatus = query.status() != null && !query.status().isBlank();

        Page<Ticket> page;
        if (byCustomer && byStatus) {
            page = repository.findByCustomerIdAndStatus(query.customerId(), query.status(), query.pageable());
        } else if (byCustomer) {
            page = repository.findByCustomerId(query.customerId(), query.pageable());
        } else if (byStatus) {
            page = repository.findByStatus(query.status(), query.pageable());
        } else {
            page = repository.findAll(query.pageable());
        }
        return new RestPage<>(page.map(mapper::toResponse));
    }
}
