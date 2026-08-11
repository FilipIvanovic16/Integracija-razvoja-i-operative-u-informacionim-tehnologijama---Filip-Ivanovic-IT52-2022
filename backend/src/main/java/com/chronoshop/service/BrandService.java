package com.chronoshop.service;

import com.chronoshop.domain.Brand;
import com.chronoshop.dto.CatalogDtos.BrandRequest;
import com.chronoshop.dto.CatalogDtos.BrandResponse;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.exception.DuplicateResourceException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.mapper.EntityMapper;
import com.chronoshop.repository.BrandRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<BrandResponse> search(String q, Pageable pageable) {
        Page<Brand> page = (q == null || q.isBlank())
                ? brandRepository.findAll(pageable)
                : brandRepository.findByNameContainingIgnoreCase(q.trim(), pageable);
        return PageResponse.from(page, EntityMapper::toBrandResponse);
    }

    @Transactional(readOnly = true)
    public BrandResponse getById(Long id) {
        return EntityMapper.toBrandResponse(findEntity(id));
    }

    public Brand findEntity(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brend", id));
    }

    @Transactional
    public BrandResponse create(BrandRequest req) {
        if (brandRepository.existsByNameIgnoreCase(req.name())) {
            throw new DuplicateResourceException("Brend '" + req.name() + "' već postoji.");
        }
        Brand b = new Brand();
        apply(b, req);
        return EntityMapper.toBrandResponse(brandRepository.save(b));
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest req) {
        Brand b = findEntity(id);
        if (!b.getName().equalsIgnoreCase(req.name()) && brandRepository.existsByNameIgnoreCase(req.name())) {
            throw new DuplicateResourceException("Brend '" + req.name() + "' već postoji.");
        }
        apply(b, req);
        return EntityMapper.toBrandResponse(brandRepository.save(b));
    }

    @Transactional
    public void delete(Long id) {
        Brand b = findEntity(id);
        brandRepository.delete(b);
    }

    private void apply(Brand b, BrandRequest req) {
        b.setName(req.name());
        b.setCountry(req.country());
        b.setDescription(req.description());
        b.setLogoUrl(req.logoUrl());
    }
}
