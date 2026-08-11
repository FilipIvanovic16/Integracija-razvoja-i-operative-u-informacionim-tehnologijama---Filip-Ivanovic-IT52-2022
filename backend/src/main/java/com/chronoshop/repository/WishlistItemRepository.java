package com.chronoshop.repository;

import com.chronoshop.domain.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserId(Long userId);

    boolean existsByUserIdAndWatchId(Long userId, Long watchId);

    Optional<WishlistItem> findByUserIdAndWatchId(Long userId, Long watchId);
}
