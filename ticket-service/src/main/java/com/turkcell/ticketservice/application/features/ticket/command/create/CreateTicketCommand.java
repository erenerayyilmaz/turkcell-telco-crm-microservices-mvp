package com.turkcell.ticketservice.application.features.ticket.command.create;

import java.time.Instant;
import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.ticketservice.dto.TicketResponse;

/** Yeni destek talebi olusturma komutu (status OPEN ile baslar). */
public record CreateTicketCommand(
        UUID customerId,
        String category,
        String priority,
        Instant slaDueAt) implements Command<TicketResponse> {
}
