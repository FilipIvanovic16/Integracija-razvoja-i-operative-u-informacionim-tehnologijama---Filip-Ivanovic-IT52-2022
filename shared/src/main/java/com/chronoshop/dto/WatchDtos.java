package com.chronoshop.dto;

import com.chronoshop.domain.enums.Documentation;
import com.chronoshop.domain.enums.Gender;
import com.chronoshop.domain.enums.MovementType;
import com.chronoshop.domain.enums.WatchCondition;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class WatchDtos {

  private WatchDtos() {}

  public record WatchRequest(
      @NotBlank @Size(max = 150) String name,
      @NotBlank @Size(max = 80) String referenceNumber,
      @NotNull Long brandId,
      @NotNull Long categoryId,
      @Size(max = 2000) String description,
      @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
      @NotNull @Min(0) Integer stockQuantity,
      MovementType movement,
      Gender gender,
      @Min(0) Integer caseDiameterMm,
      @Min(0) Integer waterResistanceM,
      List<String> imageUrls,
      Boolean active,
      WatchCondition condition,
      Documentation documentation,
      String material) {}

  public record RefName(Long id, String name) {}

  public record WatchResponse(
      Long id,
      String name,
      String referenceNumber,
      RefName brand,
      RefName category,
      String description,
      BigDecimal price,
      Integer stockQuantity,
      boolean inStock,
      MovementType movement,
      Gender gender,
      Integer caseDiameterMm,
      Integer waterResistanceM,
      String imageUrl,
      List<String> imageUrls,
      boolean active,
      WatchCondition condition,
      Documentation documentation,
      String material) {}
}
