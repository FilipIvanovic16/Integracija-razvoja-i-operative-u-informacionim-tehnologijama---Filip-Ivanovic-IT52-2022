package com.chronoshop.catalog.repository;

import com.chronoshop.catalog.domain.Category;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  boolean existsByNameIgnoreCase(String name);

  Optional<Category> findByNameIgnoreCase(String name);

  Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
