package com.turkcell.customerservice.application.features.customer.command.update;

import java.time.LocalDate;
import java.util.UUID;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.customerservice.dto.CustomerResponse;

/**
 * Musteri guncelleme komutu (id path'ten, alanlar govdeden - null alan degistirilmez).
 * status: ACTIVE | SUSPENDED | CLOSED.
 */
public record UpdateCustomerCommand(
        UUID id,
        String firstName,
        String lastName,
        String identityNumber,
        LocalDate dateOfBirth,
        String status) implements Command<CustomerResponse> {
}
