package com.chronoshop.catalog.service;

import com.chronoshop.catalog.domain.Brand;
import com.chronoshop.catalog.domain.Category;
import com.chronoshop.catalog.domain.Watch;
import com.chronoshop.catalog.domain.WatchImage;
import com.chronoshop.catalog.mapper.EntityMapper;
import com.chronoshop.catalog.repository.WatchRepository;
import com.chronoshop.catalog.repository.spec.WatchSpecifications;
import com.chronoshop.domain.enums.Documentation;
import com.chronoshop.domain.enums.Gender;
import com.chronoshop.domain.enums.MovementType;
import com.chronoshop.domain.enums.WatchCondition;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.dto.WatchDtos.WatchRequest;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.exception.DuplicateResourceException;
import com.chronoshop.exception.InsufficientStockException;
import com.chronoshop.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WatchService {

    private final WatchRepository watchRepository;
    private final BrandService brandService;
    private final CategoryService categoryService;

    public WatchService(WatchRepository watchRepository, BrandService brandService, CategoryService categoryService) {
        this.watchRepository = watchRepository;
        this.brandService = brandService;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public PageResponse<WatchResponse> search(String search, List<Long> brandId, List<Long> categoryId,
                                              List<MovementType> movement, List<Gender> gender,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              Boolean activeOnly, List<WatchCondition> condition,
                                              Boolean preOwned, List<String> material,
                                              List<Documentation> documentation, Pageable pageable) {
        Specification<Watch> spec = WatchSpecifications.build(
                search, brandId, categoryId, movement, gender, minPrice, maxPrice, activeOnly, condition, preOwned, material, documentation);
        Page<Watch> page = watchRepository.findAll(spec, pageable);
        return PageResponse.from(page, EntityMapper::toWatchResponse);
    }

    @Transactional(readOnly = true)
    public WatchResponse getById(Long id) {
        return EntityMapper.toWatchResponse(findEntity(id));
    }

    public Watch findEntity(Long id) {
        return watchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sat", id));
    }

    /**
     * Interna rezervacija/oslobadjanje zaliha - zove je order-service preko REST-a pri
     * kreiranju/otkazivanju porudzbine (delta &lt; 0 umanjuje, delta &gt; 0 vraca stanje).
     * Ovo je REST varijanta CheckStock/ReserveStock ugovora; zamenice je gRPC poziv
     * (feat/grpc-stock-check), a ova putanja ostace kao fallback.
     */
    @Transactional
    public WatchResponse adjustStock(Long id, int delta) {
        Watch w = findEntity(id);
        int newQuantity = w.getStockQuantity() + delta;
        if (newQuantity < 0) {
            throw new InsufficientStockException(w.getName(), -delta, w.getStockQuantity());
        }
        w.setStockQuantity(newQuantity);
        return EntityMapper.toWatchResponse(watchRepository.save(w));
    }

    @Transactional
    public WatchResponse create(WatchRequest req) {
        if (watchRepository.existsByReferenceNumberIgnoreCase(req.referenceNumber())) {
            throw new DuplicateResourceException("Sat sa referencom '" + req.referenceNumber() + "' već postoji.");
        }
        Watch w = new Watch();
        apply(w, req);
        return EntityMapper.toWatchResponse(watchRepository.save(w));
    }

    @Transactional
    public WatchResponse update(Long id, WatchRequest req) {
        Watch w = findEntity(id);
        if (!w.getReferenceNumber().equalsIgnoreCase(req.referenceNumber())
                && watchRepository.existsByReferenceNumberIgnoreCase(req.referenceNumber())) {
            throw new DuplicateResourceException("Sat sa referencom '" + req.referenceNumber() + "' već postoji.");
        }
        apply(w, req);
        return EntityMapper.toWatchResponse(watchRepository.save(w));
    }

    @Transactional
    public void delete(Long id) {
        Watch w = findEntity(id);
        watchRepository.delete(w);
    }

    private void apply(Watch w, WatchRequest req) {
        Brand brand = brandService.findEntity(req.brandId());
        Category category = categoryService.findEntity(req.categoryId());
        w.setName(req.name());
        w.setReferenceNumber(req.referenceNumber());
        w.setBrand(brand);
        w.setCategory(category);
        w.setDescription(req.description());
        w.setPrice(req.price());
        w.setStockQuantity(req.stockQuantity());
        w.setMovement(req.movement());
        w.setGender(req.gender());
        w.setCaseDiameterMm(req.caseDiameterMm());
        w.setWaterResistanceM(req.waterResistanceM());
        w.setActive(req.active() == null || req.active());
        w.setCondition(req.condition());
        w.setDocumentation(req.documentation());
        w.setMaterial(req.material());

        w.getImages().clear();
        if (req.imageUrls() != null) {
            int order = 0;
            for (String url : req.imageUrls()) {
                if (url != null && !url.isBlank()) {
                    WatchImage img = new WatchImage();
                    img.setWatch(w);
                    img.setUrl(url.trim());
                    img.setSortOrder(order++);
                    w.getImages().add(img);
                }
            }
        }
        w.setImageUrl(w.getImages().isEmpty() ? null : w.getImages().get(0).getUrl());
    }
}
