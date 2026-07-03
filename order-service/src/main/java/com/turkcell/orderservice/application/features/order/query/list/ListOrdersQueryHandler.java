package com.turkcell.orderservice.application.features.order.query.list;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.orderservice.application.features.order.mapper.OrderMapper;
import com.turkcell.orderservice.dto.OrderResponse;
import com.turkcell.orderservice.entity.Order;
import com.turkcell.orderservice.repository.OrderRepository;

@Component
public class ListOrdersQueryHandler implements QueryHandler<ListOrdersQuery, RestPage<OrderResponse>> {

    private final OrderRepository repository;
    private final OrderMapper mapper;

    public ListOrdersQueryHandler(OrderRepository repository, OrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RestPage<OrderResponse> handle(ListOrdersQuery query) {
        boolean byCustomer = query.customerId() != null;
        boolean byStatus = query.status() != null && !query.status().isBlank();
        String status = byStatus ? query.status().toUpperCase() : null;

        Page<Order> page;
        if (byCustomer && byStatus) {
            page = repository.findByCustomerIdAndStatus(query.customerId(), status, query.pageable());
        } else if (byCustomer) {
            page = repository.findByCustomerId(query.customerId(), query.pageable());
        } else if (byStatus) {
            page = repository.findByStatus(status, query.pageable());
        } else {
            page = repository.findAll(query.pageable());
        }
        return new RestPage<>(page.map(order -> {
            String tariffCode = order.getItems().isEmpty() ? null : order.getItems().get(0).getProductCode();
            return mapper.toResponse(order, tariffCode);
        }));
    }
}
