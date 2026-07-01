package com.turkcell.usageservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.turkcell.usageservice.entity.UsageRecord;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

    Page<UsageRecord> findBySubscriptionId(UUID subscriptionId, Pageable pageable);

    /** Bir abonelik icin [from, to) araliginda tip bazli toplam miktar ve kayit sayisi. */
    @Query("""
            select u.type as type, sum(u.quantity) as totalQuantity, count(u) as recordCount
            from UsageRecord u
            where u.subscriptionId = :subscriptionId
              and u.recordedAt >= :from and u.recordedAt < :to
            group by u.type
            order by u.type
            """)
    List<UsageTypeAggregate> aggregateByType(@Param("subscriptionId") UUID subscriptionId,
                                             @Param("from") Instant from,
                                             @Param("to") Instant to);
}
