package com.turkcell.billingservice.application.features.invoice.query.list;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.billingservice.application.features.invoice.mapper.InvoiceMapper;
import com.turkcell.billingservice.dto.InvoiceResponse;
import com.turkcell.billingservice.entity.Invoice;
import com.turkcell.billingservice.repository.InvoiceRepository;
import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.QueryHandler;

@Component
public class ListInvoicesQueryHandler implements QueryHandler<ListInvoicesQuery, RestPage<InvoiceResponse>> {

    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;

    public ListInvoicesQueryHandler(InvoiceRepository repository, InvoiceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RestPage<InvoiceResponse> handle(ListInvoicesQuery query) {
        boolean byCustomer = query.customerId() != null;
        boolean byStatus = query.status() != null && !query.status().isBlank();

        Page<Invoice> page;
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
