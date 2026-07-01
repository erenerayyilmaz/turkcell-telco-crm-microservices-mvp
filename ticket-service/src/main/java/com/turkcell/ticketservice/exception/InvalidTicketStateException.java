package com.turkcell.ticketservice.exception;

import org.springframework.http.HttpStatus;

import com.turkcell.commonlib.exception.BaseException;

/** Gecersiz durum gecisi -> 409. GlobalExceptionHandler (common-lib) yakalar. */
public class InvalidTicketStateException extends BaseException {
    public InvalidTicketStateException(String message) {
        super(message, HttpStatus.CONFLICT, "TICKET_INVALID_STATE");
    }
}
