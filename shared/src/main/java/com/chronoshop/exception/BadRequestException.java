package com.chronoshop.exception;

/**
 * Baca se za nevalidan zahtev / narušenu biznis logiku (rezultuje HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
