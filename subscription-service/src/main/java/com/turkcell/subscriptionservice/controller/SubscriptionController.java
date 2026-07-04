package com.turkcell.subscriptionservice.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Mediator;
import com.turkcell.commonlib.dto.ApiResponse;
import com.turkcell.subscriptionservice.application.features.subscription.command.reactivate.ReactivateSubscriptionCommand;
import com.turkcell.subscriptionservice.application.features.subscription.command.suspend.SuspendSubscriptionCommand;
import com.turkcell.subscriptionservice.application.features.subscription.command.terminate.TerminateSubscriptionCommand;
import com.turkcell.subscriptionservice.application.features.subscription.query.getbyid.GetSubscriptionByIdQuery;
import com.turkcell.subscriptionservice.application.features.subscription.query.list.ListSubscriptionsQuery;
import com.turkcell.subscriptionservice.dto.LifecycleActionRequest;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;

/**
 * Abonelik okuma + yasam dongusu API'si. Abonelik OLUSTURMA yolu saga'dir
 * (reserve/activate/release) — REST'ten olusturulmaz; suspend/reactivate/terminate
 * ise CSR/ADMIN'in REST islemleridir (G4, FR-14 / docx §8.4).
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

    /** Askiya alma (FR-14): ACTIVE -> SUSPENDED (CSR/ADMIN). Govde opsiyonel: {"reason": "..."}. */
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('CSR','ADMIN')")
    public ApiResponse<SubscriptionResponse> suspend(@PathVariable UUID id,
                                                     @RequestBody(required = false) LifecycleActionRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(
                mediator.send(new SuspendSubscriptionCommand(id, actorId(jwt), reason(request))),
                "Abonelik askiya alindi");
    }

    /** Yeniden aktivasyon (FR-14): SUSPENDED -> ACTIVE (CSR/ADMIN). */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('CSR','ADMIN')")
    public ApiResponse<SubscriptionResponse> reactivate(@PathVariable UUID id,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(
                mediator.send(new ReactivateSubscriptionCommand(id, actorId(jwt))),
                "Abonelik yeniden aktive edildi");
    }

    /** Sonlandirma (FR-14): ACTIVE/SUSPENDED -> TERMINATED, MSISDN havuza doner (CSR/ADMIN). */
    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAnyRole('CSR','ADMIN')")
    public ApiResponse<SubscriptionResponse> terminate(@PathVariable UUID id,
                                                       @RequestBody(required = false) LifecycleActionRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(
                mediator.send(new TerminateSubscriptionCommand(id, actorId(jwt), reason(request))),
                "Abonelik sonlandirildi");
    }

    /** Audit icin actor: JWT subject (Keycloak user id). */
    private static UUID actorId(Jwt jwt) {
        return jwt != null ? UUID.fromString(jwt.getSubject()) : null;
    }

    private static String reason(LifecycleActionRequest request) {
        return request != null ? request.reason() : null;
    }
}
