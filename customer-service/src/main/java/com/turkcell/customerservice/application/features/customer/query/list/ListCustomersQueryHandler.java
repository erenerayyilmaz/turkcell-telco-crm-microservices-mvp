package com.turkcell.customerservice.application.features.customer.query.list;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.customerservice.application.features.customer.mapper.CustomerMapper;
import com.turkcell.customerservice.dto.CustomerResponse;
import com.turkcell.customerservice.entity.Customer;
import com.turkcell.customerservice.repository.CustomerRepository;

/** Arama sonuclari degisken oldugu icin cache'lenmez (getById cache'i yeterli). */
@Component
public class ListCustomersQueryHandler implements QueryHandler<ListCustomersQuery, RestPage<CustomerResponse>> {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public ListCustomersQueryHandler(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RestPage<CustomerResponse> handle(ListCustomersQuery query) {
        Page<Customer> page = (query.q() != null && !query.q().isBlank())
                ? repository.search(query.q().trim(), query.pageable())
                : repository.findAll(query.pageable());
        return new RestPage<>(page.map(mapper::toResponse));
    }
}
