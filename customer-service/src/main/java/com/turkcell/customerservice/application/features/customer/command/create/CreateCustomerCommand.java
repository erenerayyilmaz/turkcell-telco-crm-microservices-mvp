package com.turkcell.customerservice.application.features.customer.command.create;

import java.time.LocalDate;

import com.turkcell.commonlib.cqrs.Command;
import com.turkcell.customerservice.dto.CustomerResponse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Yeni musteri olusturma komutu (HTTP request body, CSR/ADMIN). Status ACTIVE baslar. */
public record CreateCustomerCommand(
        @NotBlank @Pattern(regexp = "INDIVIDUAL|CORPORATE", message = "type: INDIVIDUAL|CORPORATE") String type,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 20) String identityNumber,
        @Past LocalDate dateOfBirth) implements Command<CustomerResponse> {
}
