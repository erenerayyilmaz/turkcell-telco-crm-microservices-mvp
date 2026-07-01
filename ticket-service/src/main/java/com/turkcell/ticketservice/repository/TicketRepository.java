package com.turkcell.ticketservice.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.ticketservice.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Page<Ticket> findByStatus(String status, Pageable pageable);

    Page<Ticket> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Ticket> findByCustomerIdAndStatus(UUID customerId, String status, Pageable pageable);
}
