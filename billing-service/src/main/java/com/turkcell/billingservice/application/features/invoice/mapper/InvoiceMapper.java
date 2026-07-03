package com.turkcell.billingservice.application.features.invoice.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.turkcell.billingservice.dto.BillCycleResponse;
import com.turkcell.billingservice.dto.InvoiceDetailResponse;
import com.turkcell.billingservice.dto.InvoiceLineResponse;
import com.turkcell.billingservice.dto.InvoiceResponse;
import com.turkcell.billingservice.entity.BillCycle;
import com.turkcell.billingservice.entity.Invoice;
import com.turkcell.billingservice.entity.InvoiceLine;

/** Invoice/InvoiceLine/BillCycle entity -> response donusumleri. */
@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice i) {
        return new InvoiceResponse(
                i.getId(), i.getCustomerId(), i.getSubscriptionId(), i.getBillCycleId(),
                i.getPeriodStart(), i.getPeriodEnd(), i.getSubTotal(), i.getTax(),
                i.getGrandTotal(), i.getStatus(), i.getDueDate(), i.getIssuedAt());
    }

    public InvoiceLineResponse toLineResponse(InvoiceLine l) {
        return new InvoiceLineResponse(l.getId(), l.getDescription(), l.getQuantity(),
                l.getUnitPrice(), l.getLineTotal());
    }

    public InvoiceDetailResponse toDetail(Invoice invoice, List<InvoiceLine> lines) {
        return new InvoiceDetailResponse(
                toResponse(invoice),
                lines.stream().map(this::toLineResponse).toList());
    }

    public BillCycleResponse toResponse(BillCycle c) {
        return new BillCycleResponse(c.getId(), c.getCustomerId(), c.getSubscriptionId(),
                c.getMonthlyFee(), c.getCurrency(), c.getDayOfMonth(), c.getNextRunDate());
    }
}
