package com.turkcell.usageservice.repository;

import java.math.BigDecimal;

/** Tip bazli agregasyon projeksiyonu (JPQL alias'lari ile eslesir). */
public interface UsageTypeAggregate {
    String getType();
    BigDecimal getTotalQuantity();
    long getRecordCount();
}
