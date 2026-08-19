package com.chronoshop.order.service;

import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.dto.WishlistDtos.WishlistItemResponse;
import com.chronoshop.exception.DuplicateResourceException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.order.client.CatalogClient;
import com.chronoshop.order.domain.WishlistItem;
import com.chronoshop.order.mapper.EntityMapper;
import com.chronoshop.order.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final CatalogClient catalogClient;

    public WishlistService(WishlistItemRepository wishlistRepository, CatalogClient catalogClient) {
        this.wishlistRepository = wishlistRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> listForUser(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(item -> EntityMapper.toWishlistItemResponse(item, catalogClient.getWatch(item.getWatchId())))
                .toList();
    }

    @Transactional
    public WishlistItemResponse add(Long userId, Long watchId) {
        if (wishlistRepository.existsByUserIdAndWatchId(userId, watchId)) {
            throw new DuplicateResourceException("Sat je već u listi želja.");
        }
        WatchResponse watch = catalogClient.getWatch(watchId);
        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setWatchId(watchId);
        return EntityMapper.toWishlistItemResponse(wishlistRepository.save(item), watch);
    }

    @Transactional
    public void remove(Long userId, Long watchId) {
        WishlistItem item = wishlistRepository.findByUserIdAndWatchId(userId, watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Stavka liste želja", watchId));
        wishlistRepository.delete(item);
    }
}
