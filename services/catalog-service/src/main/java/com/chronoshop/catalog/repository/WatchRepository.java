package com.chronoshop.catalog.repository;

import com.chronoshop.catalog.domain.Watch;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WatchRepository
    extends JpaRepository<Watch, Long>, JpaSpecificationExecutor<Watch> {

  boolean existsByReferenceNumberIgnoreCase(String referenceNumber);

  Optional<Watch> findByReferenceNumberIgnoreCase(String referenceNumber);
}
