package com.chronoshop.catalog.controller;

import com.chronoshop.catalog.service.BrandService;
import com.chronoshop.dto.CatalogDtos.BrandRequest;
import com.chronoshop.dto.CatalogDtos.BrandResponse;
import com.chronoshop.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public PageResponse<BrandResponse> search(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        return brandService.search(q, pageable);
    }

    @GetMapping("/{id}")
    public BrandResponse getById(@PathVariable Long id) {
        return brandService.getById(id);
    }

    @PostMapping
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(request));
    }

    @PutMapping("/{id}")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return brandService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
