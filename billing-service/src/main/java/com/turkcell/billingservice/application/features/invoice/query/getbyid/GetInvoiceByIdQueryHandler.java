package com.turkcell.billingservice.application.features.invoice.query.getbyid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.billingservice.application.features.invoice.mapper.InvoiceMapper;
import com.turkcell.billingservice.dto.InvoiceDetailResponse;
import com.turkcell.billingservice.entity.Invoice;
import com.turkcell.billingservice.repository.InvoiceLineRepository;
import com.turkcell.billingservice.repository.InvoiceRepository;
import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;

@Component
public class GetInvoiceByIdQueryHandler implements QueryHandler<GetInvoiceByIdQuery, InvoiceDetailResponse> {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository lineRepository;
    private final InvoiceMapper mapper;

    public GetInvoiceByIdQueryHandler(InvoiceRepository invoiceRepository,
                                      InvoiceLineRepository lineRepository,
                                      InvoiceMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.lineRepository = lineRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponse handle(GetInvoiceByIdQuery query) {
        Invoice invoice = invoiceRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", query.id().toString()));
        return mapper.toDetail(invoice, lineRepository.findByInvoiceId(invoice.getId()));
    }
}
