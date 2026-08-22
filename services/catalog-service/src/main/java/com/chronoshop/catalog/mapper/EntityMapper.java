package com.chronoshop.catalog.mapper;

import com.chronoshop.catalog.domain.Brand;
import com.chronoshop.catalog.domain.Category;
import com.chronoshop.catalog.domain.Watch;
import com.chronoshop.catalog.domain.WatchImage;
import com.chronoshop.dto.CatalogDtos.BrandResponse;
import com.chronoshop.dto.CatalogDtos.CategoryResponse;
import com.chronoshop.dto.WatchDtos.RefName;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import java.util.List;

/** Mapiranje catalog-service entiteta (Watch, Brand, Category) u DTO odgovore. */
public final class EntityMapper {

  private EntityMapper() {}

  public static BrandResponse toBrandResponse(Brand b) {
    return new BrandResponse(
        b.getId(), b.getName(), b.getCountry(), b.getDescription(), b.getLogoUrl());
  }

  public static CategoryResponse toCategoryResponse(Category c) {
    return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
  }

  public static WatchResponse toWatchResponse(Watch w) {
    List<String> imageUrls = w.getImages().stream().map(WatchImage::getUrl).toList();
    String imageUrl = imageUrls.isEmpty() ? w.getImageUrl() : imageUrls.get(0);
    return new WatchResponse(
        w.getId(),
        w.getName(),
        w.getReferenceNumber(),
        new RefName(w.getBrand().getId(), w.getBrand().getName()),
        new RefName(w.getCategory().getId(), w.getCategory().getName()),
        w.getDescription(),
        w.getPrice(),
        w.getStockQuantity(),
        w.getStockQuantity() != null && w.getStockQuantity() > 0,
        w.getMovement(),
        w.getGender(),
        w.getCaseDiameterMm(),
        w.getWaterResistanceM(),
        imageUrl,
        imageUrls,
        w.isActive(),
        w.getCondition(),
        w.getDocumentation(),
        w.getMaterial());
  }
}
