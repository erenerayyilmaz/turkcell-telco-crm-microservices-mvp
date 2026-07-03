package com.turkcell.customerservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String type,
        String firstName,
        String lastName,
        String identityNumber,
        LocalDate dateOfBirth,
        String status) {
}
