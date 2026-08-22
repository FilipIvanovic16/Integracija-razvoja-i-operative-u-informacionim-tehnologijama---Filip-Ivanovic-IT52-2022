package com.chronoshop.exception;

/**
 * Baca se kada se pokuša poručivanje veće količine nego što je dostupno na stanju. Ključni deo
 * biznis logike sistema.
 */
public class InsufficientStockException extends RuntimeException {

  public InsufficientStockException(String watchName, int requested, int available) {
    super(
        "Nedovoljno na stanju za '"
            + watchName
            + "': traženo "
            + requested
            + ", dostupno "
            + available
            + ".");
  }
}
