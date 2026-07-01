package com.turkcell.productcatalogservice.cqrs.query.handler;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.productcatalogservice.cqrs.query.model.ListTariffsQuery;
import com.turkcell.productcatalogservice.dto.TariffResponse;
import com.turkcell.productcatalogservice.entity.Tariff;
import com.turkcell.productcatalogservice.repository.TariffRepository;

@Component
public class ListTariffsQueryHandler {

    private final TariffRepository repository;

    public ListTariffsQueryHandler(TariffRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "tariffPage", key = "'p=' + #query.pageable.pageNumber + ';s=' + #query.pageable.pageSize")
    public RestPage<TariffResponse> handle(ListTariffsQuery query) {
        return new RestPage<>(repository.findAll(query.pageable()).map(ListTariffsQueryHandler::toResponse));
    }

    private static TariffResponse toResponse(Tariff t) {
        return new TariffResponse(t.getId(), t.getCode(), t.getName(), t.getType(), t.getMonthlyFee(),
                t.getMinutesIncluded(), t.getSmsIncluded(), t.getDataMbIncluded(), t.getStatus());
    }
}
