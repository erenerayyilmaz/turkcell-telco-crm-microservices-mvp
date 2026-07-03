package com.turkcell.billingservice.application.features.invoice.query.getbyid;

import java.util.UUID;

import com.turkcell.billingservice.dto.InvoiceDetailResponse;
import com.turkcell.commonlib.cqrs.Query;

/** Tekil fatura + kalemleri (BILLING_ADMIN/ADMIN). */
public record GetInvoiceByIdQuery(UUID id) implements Query<InvoiceDetailResponse> {
}
