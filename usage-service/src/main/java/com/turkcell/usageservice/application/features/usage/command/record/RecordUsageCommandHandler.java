package com.turkcell.usageservice.application.features.usage.command.record;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.usageservice.application.features.usage.mapper.UsageRecordMapper;
import com.turkcell.usageservice.dto.UsageRecordResponse;
import com.turkcell.usageservice.entity.UsageRecord;
import com.turkcell.usageservice.repository.UsageRecordRepository;

@Component
public class RecordUsageCommandHandler implements CommandHandler<RecordUsageCommand, UsageRecordResponse> {

    private final UsageRecordRepository repository;
    private final UsageRecordMapper mapper;

    public RecordUsageCommandHandler(UsageRecordRepository repository, UsageRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public UsageRecordResponse handle(RecordUsageCommand command) {
        UsageRecord saved = repository.save(mapper.toUsageRecord(command));
        return mapper.toResponse(saved);
    }
}
