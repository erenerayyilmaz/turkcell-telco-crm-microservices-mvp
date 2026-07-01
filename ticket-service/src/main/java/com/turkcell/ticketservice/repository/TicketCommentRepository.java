package com.turkcell.ticketservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.ticketservice.entity.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {

    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
