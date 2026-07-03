package com.turkcell.customerservice.application.features.customer.query.list;

import org.springframework.data.domain.Pageable;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.customerservice.dto.CustomerResponse;

/** Sayfali musteri listesi; q ile ad/soyad/TCKN icinde arama (CSR/ADMIN). */
public record ListCustomersQuery(
        Pageable pageable,
        String q) implements Query<RestPage<CustomerResponse>> {
}
