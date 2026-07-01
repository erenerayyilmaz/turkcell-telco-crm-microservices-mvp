package com.turkcell.ticketservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Talebe yorum ekleme govdesi. author, JWT 'sub'undan alinir (govdede DEGIL). */
public record AddCommentRequest(
        @NotBlank @Size(max = 2000) String body) {
}
