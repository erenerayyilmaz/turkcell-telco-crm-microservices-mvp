package com.turkcell.usageservice.application.features.usage.query.list;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.usageservice.application.features.usage.mapper.UsageRecordMapper;
import com.turkcell.usageservice.dto.UsageRecordResponse;
import com.turkcell.usageservice.repository.UsageRecordRepository;

@Component
public class ListUsageRecordsQueryHandler
        implements QueryHandler<ListUsageRecordsQuery, RestPage<UsageRecordResponse>> {

    private final UsageRecordRepository repository;
    private final UsageRecordMapper mapper;

    public ListUsageRecordsQueryHandler(UsageRecordRepository repository, UsageRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RestPage<UsageRecordResponse> handle(ListUsageRecordsQuery query) {
        return new RestPage<>(
                repository.findBySubscriptionId(query.subscriptionId(), query.pageable())
                        .map(mapper::toResponse));
    }
}
