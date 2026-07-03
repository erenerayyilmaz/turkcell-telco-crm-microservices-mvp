package com.turkcell.billingservice.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.turkcell.billingservice.entity.BillCycle;

public interface BillCycleRepository extends JpaRepository<BillCycle, UUID> {

    Page<BillCycle> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Vadesi gelen fatura dongulerini kilitleyerek alir (FOR UPDATE SKIP LOCKED):
     * bill-run coklu instance'da calisirsa herkes FARKLI donguleri isler, ayni
     * doneme iki fatura kesilmez. subscription_id/monthly_fee NULL olan eski
     * (V3 oncesi) satirlar atlanir.
     */
    @Query(value = """
            SELECT * FROM bill_cycles
            WHERE next_run_date <= :today
              AND subscription_id IS NOT NULL
              AND monthly_fee IS NOT NULL
            ORDER BY next_run_date
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<BillCycle> findDue(@Param("today") LocalDate today, @Param("limit") int limit);
}
