package com.turkcell.ticketservice.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** Talebi bir CSR'e atama govdesi. assigneeId = atanacak kullanicinin Keycloak sub'i (UUID). */
public record AssignTicketRequest(
        @NotNull UUID assigneeId) {
}
