package com.turkcell.usageservice.application.features.usage.mapper;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.turkcell.usageservice.application.features.usage.command.record.RecordUsageCommand;
import com.turkcell.usageservice.dto.UsageRecordResponse;
import com.turkcell.usageservice.entity.UsageRecord;
import com.turkcell.usageservice.event.UsageRecordedEvent;

/** UsageRecord entity <-> command/event/response donusumleri. */
@Component
public class UsageRecordMapper {

    /** REST ingestion komutundan kayit (recordedAt yoksa now). */
    public UsageRecord toUsageRecord(RecordUsageCommand c) {
        UsageRecord r = new UsageRecord();
        r.setSubscriptionId(c.subscriptionId());
        r.setType(c.type());
        r.setQuantity(c.quantity());
        r.setRecordedAt(c.recordedAt() != null ? c.recordedAt() : Instant.now());
        r.setCdrRef(c.cdrRef());
        return r;
    }

    /** Kafka usage event'inden kayit (recordedAt yoksa now). */
    public UsageRecord fromEvent(UsageRecordedEvent e) {
        UsageRecord r = new UsageRecord();
        r.setSubscriptionId(e.subscriptionId());
        r.setType(e.type());
        r.setQuantity(e.quantity());
        r.setRecordedAt(e.recordedAt() != null ? e.recordedAt() : Instant.now());
        r.setCdrRef(e.cdrRef());
        return r;
    }

    public UsageRecordResponse toResponse(UsageRecord r) {
        return new UsageRecordResponse(
                r.getId(), r.getSubscriptionId(), r.getType(), r.getQuantity(),
                r.getRecordedAt(), r.getCdrRef());
    }
}
