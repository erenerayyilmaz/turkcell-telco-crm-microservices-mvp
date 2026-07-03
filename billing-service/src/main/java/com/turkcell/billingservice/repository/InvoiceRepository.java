package com.turkcell.billingservice.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.billingservice.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Invoice> findByStatus(String status, Pageable pageable);

    Page<Invoice> findByCustomerIdAndStatus(UUID customerId, String status, Pageable pageable);
}
