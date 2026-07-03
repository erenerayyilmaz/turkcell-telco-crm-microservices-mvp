package com.turkcell.billingservice.application.features.billcycle.query.list;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.turkcell.billingservice.dto.BillCycleResponse;
import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Query;

/** Sayfali fatura dongusu listesi; customerId ile opsiyonel filtre (BILLING_ADMIN/ADMIN). */
public record ListBillCyclesQuery(
        Pageable pageable,
        UUID customerId) implements Query<RestPage<BillCycleResponse>> {
}
