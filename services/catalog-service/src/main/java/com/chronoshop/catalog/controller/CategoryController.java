package com.chronoshop.catalog.controller;

import com.chronoshop.catalog.service.CategoryService;
import com.chronoshop.dto.CatalogDtos.CategoryRequest;
import com.chronoshop.dto.CatalogDtos.CategoryResponse;
import com.chronoshop.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping
  public PageResponse<CategoryResponse> search(
      @RequestParam(required = false) String q,
      @PageableDefault(size = 50, sort = "name") Pageable pageable) {
    return categoryService.search(q, pageable);
  }

  @GetMapping("/{id}")
  public CategoryResponse getById(@PathVariable Long id) {
    return categoryService.getById(id);
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
  }

  @PutMapping("/{id}")
  public CategoryResponse update(
      @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
    return categoryService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    categoryService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
