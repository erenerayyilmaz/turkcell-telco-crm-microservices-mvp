package com.turkcell.billingservice.dto;

import java.util.List;

/** Tekil fatura gorunumu: fatura + kalemleri. */
public record InvoiceDetailResponse(
        InvoiceResponse invoice,
        List<InvoiceLineResponse> lines) {
}
