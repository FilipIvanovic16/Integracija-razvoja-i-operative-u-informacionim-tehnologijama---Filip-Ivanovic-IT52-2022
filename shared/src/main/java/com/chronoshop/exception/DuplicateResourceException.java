package com.chronoshop.exception;

/**
 * Baca se pri pokušaju kreiranja resursa koji narušava jedinstvenost (rezultuje HTTP 409).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
