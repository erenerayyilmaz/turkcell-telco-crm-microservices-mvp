package com.turkcell.subscriptionservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.turkcell.subscriptionservice.entity.AuditLog;
import com.turkcell.subscriptionservice.repository.AuditLogRepository;

/**
 * audit_log yazim yardimcisi (docx §13: subscription'da her degisiklik audit'lenir).
 * Saga tarafinin aksine REST yasam dongusu islemlerinde actor (JWT subject) bellidir.
 */
@Component
public class AuditWriter {

    private final AuditLogRepository auditLogRepository;

    public AuditWriter(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void write(UUID actorUserId, String entityName, UUID entityId, String action, String detail) {
        AuditLog a = new AuditLog();
        a.setActorUserId(actorUserId);
        a.setServiceName("subscription-service");
        a.setEntityName(entityName);
        a.setEntityId(entityId);
        a.setAction(action);
        a.setDetail(detail);
        a.setCreatedAt(Instant.now());
        auditLogRepository.save(a);
    }
}
