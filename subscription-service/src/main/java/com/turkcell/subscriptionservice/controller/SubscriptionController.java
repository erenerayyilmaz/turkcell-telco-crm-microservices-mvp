package com.turkcell.subscriptionservice.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Mediator;
import com.turkcell.commonlib.dto.ApiResponse;
import com.turkcell.subscriptionservice.application.features.subscription.query.getbyid.GetSubscriptionByIdQuery;
import com.turkcell.subscriptionservice.application.features.subscription.query.list.ListSubscriptionsQuery;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;

/**
 * Abonelik okuma API'si (FE subscriptions sayfasi). Yazma yolu saga'dir
 * (reserve/activate/release) — REST'ten abonelik olusturulmaz/degistirilmez.
 */
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final Mediator mediator;

    public SubscriptionController(Mediator mediator) {
        this.mediator = mediator;
    }

    /** Sayfali abonelik listesi; customerId/status filtreli (CSR/ADMIN). */
    @GetMapping
    @PreAuthorize("hasAnyRole('CSR','ADMIN')")
    public ApiResponse<RestPage<SubscriptionResponse>> list(Pageable pageable,
                                                            @RequestParam(required = false) UUID customerId,
                                                            @RequestParam(required = false) String status) {
        return ApiResponse.ok(mediator.send(new ListSubscriptionsQuery(pageable, customerId, status)));
    }

    /** Tekil abonelik (CSR/ADMIN). */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CSR','ADMIN')")
    public ApiResponse<SubscriptionResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(mediator.send(new GetSubscriptionByIdQuery(id)));
    }
}
