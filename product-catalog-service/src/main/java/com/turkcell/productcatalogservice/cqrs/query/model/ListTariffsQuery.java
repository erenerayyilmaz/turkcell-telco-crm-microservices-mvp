package com.turkcell.productcatalogservice.cqrs.query.model;

import org.springframework.data.domain.Pageable;

public record ListTariffsQuery(Pageable pageable) {}
