package com.turkcell.usageservice.application.features.usage.query.summary;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.QueryHandler;
import com.turkcell.usageservice.dto.UsageSummaryResponse;
import com.turkcell.usageservice.dto.UsageTypeSummary;
import com.turkcell.usageservice.repository.UsageRecordRepository;
import com.turkcell.usageservice.repository.UsageTypeAggregate;

@Component
public class GetSubscriptionUsageSummaryQueryHandler
        implements QueryHandler<GetSubscriptionUsageSummaryQuery, UsageSummaryResponse> {

    private final UsageRecordRepository repository;

    public GetSubscriptionUsageSummaryQueryHandler(UsageRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UsageSummaryResponse handle(GetSubscriptionUsageSummaryQuery query) {
        List<UsageTypeAggregate> aggregates =
                repository.aggregateByType(query.subscriptionId(), query.from(), query.to());

        List<UsageTypeSummary> byType = aggregates.stream()
                .map(a -> new UsageTypeSummary(a.getType(), a.getTotalQuantity(), a.getRecordCount()))
                .toList();
        long totalRecords = byType.stream().mapToLong(UsageTypeSummary::recordCount).sum();

        return new UsageSummaryResponse(
                query.subscriptionId(), query.from(), query.to(), byType, totalRecords);
    }
}
