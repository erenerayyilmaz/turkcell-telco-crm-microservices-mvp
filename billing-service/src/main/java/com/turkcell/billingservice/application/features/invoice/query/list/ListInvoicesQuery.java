package com.turkcell.billingservice.application.features.invoice.query.list;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.billingservice.dto.InvoiceResponse;

/** Sayfali fatura listesi; customerId ve/veya status ile opsiyonel filtre (BILLING_ADMIN/ADMIN). */
public record ListInvoicesQuery(
        Pageable pageable,
        UUID customerId,
        String status) implements Query<RestPage<InvoiceResponse>> {
}
