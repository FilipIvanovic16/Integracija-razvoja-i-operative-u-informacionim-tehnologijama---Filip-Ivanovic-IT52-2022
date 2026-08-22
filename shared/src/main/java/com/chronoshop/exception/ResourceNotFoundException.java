package com.chronoshop.exception;

/** Baca se kada traženi resurs ne postoji (rezultuje HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String resource, Object id) {
    super(resource + " sa identifikatorom '" + id + "' nije pronađen.");
  }
}
