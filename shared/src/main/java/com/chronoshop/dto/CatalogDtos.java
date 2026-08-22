package com.chronoshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CatalogDtos {

  private CatalogDtos() {}

  public record BrandRequest(
      @NotBlank @Size(max = 100) String name,
      @Size(max = 80) String country,
      @Size(max = 1000) String description,
      @Size(max = 500) String logoUrl) {}

  public record BrandResponse(
      Long id, String name, String country, String description, String logoUrl) {}

  public record CategoryRequest(
      @NotBlank @Size(max = 100) String name, @Size(max = 500) String description) {}

  public record CategoryResponse(Long id, String name, String description) {}

  /**
   * Interni zahtev order-service -> catalog-service za rezervaciju/oslobadjanje zaliha. Negativan
   * delta umanjuje stanje (rezervacija pri kreiranju porudzbine), pozitivan ga vraca (otkazana
   * porudzbina). Zamenice se gRPC ReserveStock pozivom (vidi PR feat/grpc-stock-check), ova REST
   * putanja ostaje kao fallback.
   */
  public record StockAdjustmentRequest(@NotNull Integer delta) {}
}
