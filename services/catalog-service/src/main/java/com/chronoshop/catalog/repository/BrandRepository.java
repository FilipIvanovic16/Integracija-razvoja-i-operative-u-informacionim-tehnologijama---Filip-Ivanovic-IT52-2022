package com.chronoshop.catalog.repository;

import com.chronoshop.catalog.domain.Brand;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

  boolean existsByNameIgnoreCase(String name);

  Optional<Brand> findByNameIgnoreCase(String name);

  Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
