package com.turkcell.subscriptionservice.exception;

import org.springframework.http.HttpStatus;

import com.turkcell.commonlib.exception.BaseException;

/**
 * Gecersiz abonelik durum gecisi (or. SUSPENDED olmayan aboneligi reactivate etmek) -> 409.
 * GlobalExceptionHandler (common-lib) yakalar.
 */
public class InvalidSubscriptionStateException extends BaseException {
    public InvalidSubscriptionStateException(String message) {
        super(message, HttpStatus.CONFLICT, "SUBSCRIPTION_INVALID_STATE");
    }
}
