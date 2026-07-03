package com.turkcell.billingservice.application.features.billcycle.query.list;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.billingservice.application.features.invoice.mapper.InvoiceMapper;
import com.turkcell.billingservice.dto.BillCycleResponse;
import com.turkcell.billingservice.entity.BillCycle;
import com.turkcell.billingservice.repository.BillCycleRepository;
import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.QueryHandler;

@Component
public class ListBillCyclesQueryHandler implements QueryHandler<ListBillCyclesQuery, RestPage<BillCycleResponse>> {

    private final BillCycleRepository repository;
    private final InvoiceMapper mapper;

    public ListBillCyclesQueryHandler(BillCycleRepository repository, InvoiceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RestPage<BillCycleResponse> handle(ListBillCyclesQuery query) {
        Page<BillCycle> page = query.customerId() != null
                ? repository.findByCustomerId(query.customerId(), query.pageable())
                : repository.findAll(query.pageable());
        return new RestPage<>(page.map(mapper::toResponse));
    }
}
