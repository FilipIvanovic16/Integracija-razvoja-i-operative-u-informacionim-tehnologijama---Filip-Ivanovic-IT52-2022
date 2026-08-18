package com.chronoshop.catalog.controller;

import com.chronoshop.catalog.service.WatchService;
import com.chronoshop.domain.enums.Documentation;
import com.chronoshop.domain.enums.Gender;
import com.chronoshop.domain.enums.MovementType;
import com.chronoshop.domain.enums.WatchCondition;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.dto.WatchDtos.WatchRequest;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Katalog satova.
 */
@RestController
@RequestMapping("/api/watches")
public class WatchController {

    private final WatchService watchService;

    public WatchController(WatchService watchService) {
        this.watchService = watchService;
    }

    /**
     * Pretraga sa filterima, paginacijom i sortiranjem.
     */
    @GetMapping
    public PageResponse<WatchResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> brandId,
            @RequestParam(required = false) List<Long> categoryId,
            @RequestParam(required = false) List<MovementType> movement,
            @RequestParam(required = false) List<Gender> gender,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "true") Boolean activeOnly,
            @RequestParam(required = false) List<WatchCondition> condition,
            @RequestParam(required = false) Boolean preOwned,
            @RequestParam(required = false) List<String> material,
            @RequestParam(required = false) List<Documentation> documentation,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {
        return watchService.search(search, brandId, categoryId, movement, gender, minPrice, maxPrice, activeOnly, condition, preOwned, material, documentation, pageable);
    }

    @GetMapping("/{id}")
    public WatchResponse getById(@PathVariable Long id) {
        return watchService.getById(id);
    }

    @PostMapping
    public ResponseEntity<WatchResponse> create(@Valid @RequestBody WatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(watchService.create(request));
    }

    @PutMapping("/{id}")
    public WatchResponse update(@PathVariable Long id, @Valid @RequestBody WatchRequest request) {
        return watchService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        watchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
