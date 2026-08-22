package com.chronoshop.exception;

import com.chronoshop.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Globalno rukovanje izuzecima (@ControllerAdvice) — klijent uvek dobija uniforman, bezbedan JSON
 * odgovor bez otkrivanja internih detalja sistema.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      ResourceNotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiError> handleDuplicate(
      DuplicateResourceException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), req);
  }

  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ApiError> handleStock(
      InsufficientStockException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), req);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentials(
      BadCredentialsException ex, HttpServletRequest req) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest req) {
    return build(HttpStatus.FORBIDDEN, "Nemate ovlašćenje za pristup ovom resursu.", req);
  }

  /** Greške validacije @Valid tela zahteva — vraćaju mapu polje → poruka. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fe.getField(), fe.getDefaultMessage());
    }
    ApiError error =
        ApiError.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Zahtev sadrži nevalidne podatke.",
            req.getRequestURI(),
            fieldErrors);
    return ResponseEntity.badRequest().body(error);
  }

  /** Poslednja linija odbrane — neočekivane greške. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Došlo je do neočekivane greške. Pokušajte ponovo kasnije.",
        req);
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status, String message, HttpServletRequest req) {
    ApiError error =
        ApiError.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI());
    return ResponseEntity.status(status).body(error);
  }
}
