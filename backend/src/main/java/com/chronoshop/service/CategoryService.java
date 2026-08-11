package com.chronoshop.service;

import com.chronoshop.domain.Category;
import com.chronoshop.dto.CatalogDtos.CategoryRequest;
import com.chronoshop.dto.CatalogDtos.CategoryResponse;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.exception.DuplicateResourceException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.mapper.EntityMapper;
import com.chronoshop.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> search(String q, Pageable pageable) {
        Page<Category> page = (q == null || q.isBlank())
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByNameContainingIgnoreCase(q.trim(), pageable);
        return PageResponse.from(page, EntityMapper::toCategoryResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return EntityMapper.toCategoryResponse(findEntity(id));
    }

    public Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategorija", id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.name())) {
            throw new DuplicateResourceException("Kategorija '" + req.name() + "' već postoji.");
        }
        Category c = new Category();
        c.setName(req.name());
        c.setDescription(req.description());
        return EntityMapper.toCategoryResponse(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req) {
        Category c = findEntity(id);
        if (!c.getName().equalsIgnoreCase(req.name()) && categoryRepository.existsByNameIgnoreCase(req.name())) {
            throw new DuplicateResourceException("Kategorija '" + req.name() + "' već postoji.");
        }
        c.setName(req.name());
        c.setDescription(req.description());
        return EntityMapper.toCategoryResponse(categoryRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        Category c = findEntity(id);
        categoryRepository.delete(c);
    }
}
