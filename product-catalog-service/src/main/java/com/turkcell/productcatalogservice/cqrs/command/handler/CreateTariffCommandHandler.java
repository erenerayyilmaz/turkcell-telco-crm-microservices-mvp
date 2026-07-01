package com.turkcell.productcatalogservice.cqrs.command.handler;

import java.time.LocalDate;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import com.turkcell.productcatalogservice.cqrs.command.model.CreateTariffCommand;
import com.turkcell.productcatalogservice.dto.TariffResponse;
import com.turkcell.productcatalogservice.entity.Tariff;
import com.turkcell.productcatalogservice.repository.TariffRepository;

@Component
public class CreateTariffCommandHandler {

    private final TariffRepository repository;

    public CreateTariffCommandHandler(TariffRepository repository) {
        this.repository = repository;
    }

    @Caching(evict = {
            @CacheEvict(value = "tariffByCode", allEntries = true),
            @CacheEvict(value = "tariffPage", allEntries = true)
    })
    public TariffResponse handle(CreateTariffCommand command) {
        Tariff tariff = new Tariff();
        tariff.setCode(command.code());
        tariff.setName(command.name());
        tariff.setType(command.type());
        tariff.setMonthlyFee(command.monthlyFee());
        tariff.setMinutesIncluded(command.minutesIncluded());
        tariff.setSmsIncluded(command.smsIncluded());
        tariff.setDataMbIncluded(command.dataMbIncluded());
        tariff.setStatus("ACTIVE");
        tariff.setEffectiveFrom(LocalDate.now());

        Tariff saved = repository.save(tariff);
        return toResponse(saved);
    }

    private static TariffResponse toResponse(Tariff t) {
        return new TariffResponse(t.getId(), t.getCode(), t.getName(), t.getType(), t.getMonthlyFee(),
                t.getMinutesIncluded(), t.getSmsIncluded(), t.getDataMbIncluded(), t.getStatus());
    }
}
