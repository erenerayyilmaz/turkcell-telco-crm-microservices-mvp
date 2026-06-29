package com.turkcell.productcatalogservice.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.dto.ApiResponse;
import com.turkcell.productcatalogservice.cqrs.command.handler.CreateTariffCommandHandler;
import com.turkcell.productcatalogservice.cqrs.command.model.CreateTariffCommand;
import com.turkcell.productcatalogservice.cqrs.query.handler.GetTariffByCodeQueryHandler;
import com.turkcell.productcatalogservice.cqrs.query.handler.ListTariffsQueryHandler;
import com.turkcell.productcatalogservice.cqrs.query.model.GetTariffByCodeQuery;
import com.turkcell.productcatalogservice.cqrs.query.model.ListTariffsQuery;
import com.turkcell.productcatalogservice.dto.CreateTariffRequest;
import com.turkcell.productcatalogservice.dto.TariffResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalog/tariffs")
public class CatalogController {

    private final CreateTariffCommandHandler createTariffCommandHandler;
    private final GetTariffByCodeQueryHandler getTariffByCodeQueryHandler;
    private final ListTariffsQueryHandler listTariffsQueryHandler;

    public CatalogController(CreateTariffCommandHandler createTariffCommandHandler,
                             GetTariffByCodeQueryHandler getTariffByCodeQueryHandler,
                             ListTariffsQueryHandler listTariffsQueryHandler) {
        this.createTariffCommandHandler = createTariffCommandHandler;
        this.getTariffByCodeQueryHandler = getTariffByCodeQueryHandler;
        this.listTariffsQueryHandler = listTariffsQueryHandler;
    }

    // Her kimlik dogrulanmis kullanici tarifeleri okuyabilir (Redis cache'li).
    @GetMapping("/{code}")
    public ApiResponse<TariffResponse> getByCode(@PathVariable String code) {
        return ApiResponse.ok(getTariffByCodeQueryHandler.handle(new GetTariffByCodeQuery(code)));
    }

    @GetMapping
    public ApiResponse<RestPage<TariffResponse>> list(Pageable pageable) {
        return ApiResponse.ok(listTariffsQueryHandler.handle(new ListTariffsQuery(pageable)));
    }

    // Yalnizca CATALOG_ADMIN tarife olusturabilir.
    @PostMapping
    @PreAuthorize("hasRole('CATALOG_ADMIN')")
    public ApiResponse<TariffResponse> create(@Valid @RequestBody CreateTariffRequest request) {
        CreateTariffCommand command = new CreateTariffCommand(
                request.code(),
                request.name(),
                request.type(),
                request.monthlyFee(),
                request.minutesIncluded(),
                request.smsIncluded(),
                request.dataMbIncluded()
        );
        return ApiResponse.ok(createTariffCommandHandler.handle(command), "Tarife olusturuldu");
    }
}
