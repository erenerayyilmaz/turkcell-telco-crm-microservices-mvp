package com.turkcell.customerservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Musteri guncelleme govdesi (id path'ten gelir; null alan degistirilmez). */
public record UpdateCustomerRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 20) String identityNumber,
        @Past LocalDate dateOfBirth,
        @Pattern(regexp = "ACTIVE|SUSPENDED|CLOSED", message = "status: ACTIVE|SUSPENDED|CLOSED") String status) {
}
