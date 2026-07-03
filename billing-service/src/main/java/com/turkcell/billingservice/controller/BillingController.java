package com.turkcell.billingservice.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turkcell.billingservice.application.features.billcycle.query.list.ListBillCyclesQuery;
import com.turkcell.billingservice.application.features.invoice.query.getbyid.GetInvoiceByIdQuery;
import com.turkcell.billingservice.application.features.invoice.query.list.ListInvoicesQuery;
import com.turkcell.billingservice.dto.BillCycleResponse;
import com.turkcell.billingservice.dto.InvoiceDetailResponse;
import com.turkcell.billingservice.dto.InvoiceResponse;
import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Mediator;
import com.turkcell.commonlib.dto.ApiResponse;

/** Faturalama okuma API'si (FE billing sayfasi). Yazma yolu event-driven'dir (bill-run + Kafka). */
@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final Mediator mediator;

    public BillingController(Mediator mediator) {
        this.mediator = mediator;
    }

    /** Sayfali fatura listesi; customerId/status filtreli (BILLING_ADMIN/ADMIN, CSR okuyabilir). */
    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('BILLING_ADMIN','CSR','ADMIN')")
    public ApiResponse<RestPage<InvoiceResponse>> listInvoices(Pageable pageable,
                                                               @RequestParam(required = false) UUID customerId,
                                                               @RequestParam(required = false) String status) {
        return ApiResponse.ok(mediator.send(new ListInvoicesQuery(pageable, customerId, status)));
    }

    /** Tekil fatura + kalemleri. */
    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('BILLING_ADMIN','CSR','ADMIN')")
    public ApiResponse<InvoiceDetailResponse> getInvoice(@PathVariable UUID id) {
        return ApiResponse.ok(mediator.send(new GetInvoiceByIdQuery(id)));
    }

    /** Sayfali fatura dongusu listesi; customerId filtreli. */
    @GetMapping("/bill-cycles")
    @PreAuthorize("hasAnyRole('BILLING_ADMIN','CSR','ADMIN')")
    public ApiResponse<RestPage<BillCycleResponse>> listBillCycles(Pageable pageable,
                                                                   @RequestParam(required = false) UUID customerId) {
        return ApiResponse.ok(mediator.send(new ListBillCyclesQuery(pageable, customerId)));
    }
}
