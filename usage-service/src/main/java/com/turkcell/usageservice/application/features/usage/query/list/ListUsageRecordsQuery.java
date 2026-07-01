package com.turkcell.usageservice.application.features.usage.query.list;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.turkcell.commonlib.cache.RestPage;
import com.turkcell.commonlib.cqrs.Query;
import com.turkcell.usageservice.dto.UsageRecordResponse;

/** Bir abonelige ait sayfali ham kullanim kayitlari. */
public record ListUsageRecordsQuery(
        UUID subscriptionId,
        Pageable pageable) implements Query<RestPage<UsageRecordResponse>> {
}
