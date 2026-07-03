package com.turkcell.orderservice.application.features.order.query.list;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.orderservice.dto.OrderResponse;

/** Sayfali siparis listesi; customerId ve/veya status ile opsiyonel filtre (CSR/ADMIN). */
public record ListOrdersQuery(
        Pageable pageable,
        UUID customerId,
        String status) implements Query<RestPage<OrderResponse>> {
}
