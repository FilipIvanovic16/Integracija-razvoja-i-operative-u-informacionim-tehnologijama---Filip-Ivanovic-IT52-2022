package com.chronoshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record BrandRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 80) String country,
            @Size(max = 1000) String description,
            @Size(max = 500) String logoUrl
    ) {
    }

    public record BrandResponse(
            Long id,
            String name,
            String country,
            String description,
            String logoUrl
    ) {
    }

    public record CategoryRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description
    ) {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String description
    ) {
    }
}
