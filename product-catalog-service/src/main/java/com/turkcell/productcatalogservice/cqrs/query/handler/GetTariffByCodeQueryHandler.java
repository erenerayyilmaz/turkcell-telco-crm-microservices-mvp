package com.turkcell.productcatalogservice.cqrs.query.handler;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.productcatalogservice.cqrs.query.model.GetTariffByCodeQuery;
import com.turkcell.productcatalogservice.dto.TariffResponse;
import com.turkcell.productcatalogservice.entity.Tariff;
import com.turkcell.productcatalogservice.repository.TariffRepository;

@Component
public class GetTariffByCodeQueryHandler {

    private final TariffRepository repository;

    public GetTariffByCodeQueryHandler(TariffRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "tariffByCode", key = "#query.code")
    public TariffResponse handle(GetTariffByCodeQuery query) {
        Tariff tariff = repository.findByCode(query.code())
                .orElseThrow(() -> new ResourceNotFoundException("Tariff", query.code()));
        return toResponse(tariff);
    }

    private static TariffResponse toResponse(Tariff t) {
        return new TariffResponse(t.getId(), t.getCode(), t.getName(), t.getType(), t.getMonthlyFee(),
                t.getMinutesIncluded(), t.getSmsIncluded(), t.getDataMbIncluded(), t.getStatus());
    }
}
