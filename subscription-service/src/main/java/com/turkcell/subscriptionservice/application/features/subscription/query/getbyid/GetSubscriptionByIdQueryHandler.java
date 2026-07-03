package com.turkcell.subscriptionservice.application.features.subscription.query.getbyid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.subscriptionservice.application.features.subscription.mapper.SubscriptionMapper;
import com.turkcell.subscriptionservice.dto.SubscriptionResponse;
import com.turkcell.subscriptionservice.repository.SubscriptionRepository;

@Component
public class GetSubscriptionByIdQueryHandler
        implements QueryHandler<GetSubscriptionByIdQuery, SubscriptionResponse> {

    private final SubscriptionRepository repository;
    private final SubscriptionMapper mapper;

    public GetSubscriptionByIdQueryHandler(SubscriptionRepository repository, SubscriptionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse handle(GetSubscriptionByIdQuery query) {
        return repository.findById(query.id())
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", query.id().toString()));
    }
}
