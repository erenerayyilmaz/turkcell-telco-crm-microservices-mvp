package com.turkcell.ticketservice.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Yeni destek talebi olusturma govdesi (CSR/ADMIN). */
public record CreateTicketRequest(
        @NotNull UUID customerId,
        @NotBlank @Size(max = 100) String category,
        @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT", message = "priority: LOW|MEDIUM|HIGH|URGENT")
        String priority,
        Instant slaDueAt) {
}
