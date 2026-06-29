package com.turkcell.productcatalogservice.cqrs.command.model;

import java.math.BigDecimal;

public record CreateTariffCommand(
    String code,
    String name,
    String type,
    BigDecimal monthlyFee,
    Integer minutesIncluded,
    Integer smsIncluded,
    Integer dataMbIncluded
) {}
