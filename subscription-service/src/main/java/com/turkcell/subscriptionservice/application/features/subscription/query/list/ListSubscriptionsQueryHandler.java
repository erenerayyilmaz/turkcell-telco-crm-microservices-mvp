package com.turkcell.subscriptionservice.application.features.subscription.query.list;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.subscriptionservice.application.features.subscription.mapper.SubscriptionMapper;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.entity.Subscription;
import com.turkcell.subscriptionservice.repository.SubscriptionRepository;

@Component
public class ListSubscriptionsQueryHandler
        implements QueryHandler<ListSubscriptionsQuery, RestPage<SubscriptionResponse>> {

    private final SubscriptionRepository repository;
    private final SubscriptionMapper mapper;

    public ListSubscriptionsQueryHandler(SubscriptionRepository repository, SubscriptionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RestPage<SubscriptionResponse> handle(ListSubscriptionsQuery query) {
        boolean byCustomer = query.customerId() != null;
        boolean byStatus = query.status() != null && !query.status().isBlank();

        Page<Subscription> page;
        if (byCustomer && byStatus) {
            page = repository.findByCustomerIdAndStatus(query.customerId(), query.status(), query.pageable());
        } else if (byCustomer) {
            page = repository.findByCustomerId(query.customerId(), query.pageable());
        } else if (byStatus) {
            page = repository.findByStatus(query.status(), query.pageable());
        } else {
            page = repository.findAll(query.pageable());
        }
        return new RestPage<>(page.map(mapper::toResponse));
    }
}
