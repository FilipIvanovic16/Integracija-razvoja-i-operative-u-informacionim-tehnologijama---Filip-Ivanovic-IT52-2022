package com.chronoshop.service;

import com.chronoshop.domain.User;
import com.chronoshop.domain.Watch;
import com.chronoshop.domain.WishlistItem;
import com.chronoshop.dto.WishlistDtos.WishlistItemResponse;
import com.chronoshop.exception.DuplicateResourceException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.mapper.EntityMapper;
import com.chronoshop.repository.UserRepository;
import com.chronoshop.repository.WatchRepository;
import com.chronoshop.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final UserRepository userRepository;
    private final WatchRepository watchRepository;

    public WishlistService(WishlistItemRepository wishlistRepository, UserRepository userRepository,
                           WatchRepository watchRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.watchRepository = watchRepository;
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> listForUser(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(EntityMapper::toWishlistItemResponse).toList();
    }

    @Transactional
    public WishlistItemResponse add(Long userId, Long watchId) {
        if (wishlistRepository.existsByUserIdAndWatchId(userId, watchId)) {
            throw new DuplicateResourceException("Sat je već u listi želja.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik", userId));
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Sat", watchId));
        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setWatch(watch);
        return EntityMapper.toWishlistItemResponse(wishlistRepository.save(item));
    }

    @Transactional
    public void remove(Long userId, Long watchId) {
        WishlistItem item = wishlistRepository.findByUserIdAndWatchId(userId, watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Stavka liste želja", watchId));
        wishlistRepository.delete(item);
    }
}
