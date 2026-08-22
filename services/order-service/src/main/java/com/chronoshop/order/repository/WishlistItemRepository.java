package com.chronoshop.order.repository;

import com.chronoshop.order.domain.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

  List<WishlistItem> findByUserId(Long userId);

  boolean existsByUserIdAndWatchId(Long userId, Long watchId);

  Optional<WishlistItem> findByUserIdAndWatchId(Long userId, Long watchId);
}
