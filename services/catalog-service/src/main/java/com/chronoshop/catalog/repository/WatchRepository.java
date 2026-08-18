package com.chronoshop.catalog.repository;

import com.chronoshop.catalog.domain.Watch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WatchRepository extends JpaRepository<Watch, Long>, JpaSpecificationExecutor<Watch> {

    boolean existsByReferenceNumberIgnoreCase(String referenceNumber);

    Optional<Watch> findByReferenceNumberIgnoreCase(String referenceNumber);
}
