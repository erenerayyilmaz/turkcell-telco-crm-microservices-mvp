package com.turkcell.orderservice.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.orderservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByStatus(String status, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(UUID customerId, String status, Pageable pageable);
}
