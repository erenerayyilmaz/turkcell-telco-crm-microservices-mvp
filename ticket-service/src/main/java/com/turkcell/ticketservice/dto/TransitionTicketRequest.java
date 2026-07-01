package com.turkcell.ticketservice.dto;

import jakarta.validation.constraints.NotBlank;

/** Durum gecisi govdesi (CSR/ADMIN). Gecerlilik + izinli gecis TicketBusinessRules'ta denetlenir. */
public record TransitionTicketRequest(
        @NotBlank String targetStatus) {
}
