package com.chronoshop.order.controller;

import com.chronoshop.dto.WishlistDtos.AddWishlistRequest;
import com.chronoshop.dto.WishlistDtos.WishlistItemResponse;
import com.chronoshop.order.security.SecurityUtils;
import com.chronoshop.order.service.WishlistService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Lista zelja ulogovanog korisnika. Ostaje na istoj putanji kao u monolitu (/api/account/wishlist)
 * da frontend ne mora da se menja - gateway (PR feat/api-gateway) rutira ovu putanju ka
 * order-service, a /api/account/me i /api/account/addresses/** ka auth-service.
 */
@RestController
@RequestMapping("/api/account/wishlist")
public class WishlistController {

  private final WishlistService wishlistService;

  public WishlistController(WishlistService wishlistService) {
    this.wishlistService = wishlistService;
  }

  @GetMapping
  public List<WishlistItemResponse> wishlist() {
    return wishlistService.listForUser(SecurityUtils.currentUserId());
  }

  @PostMapping
  public ResponseEntity<WishlistItemResponse> addToWishlist(
      @Valid @RequestBody AddWishlistRequest request) {
    WishlistItemResponse res =
        wishlistService.add(SecurityUtils.currentUserId(), request.watchId());
    return ResponseEntity.status(HttpStatus.CREATED).body(res);
  }

  @DeleteMapping("/{watchId}")
  public ResponseEntity<Void> removeFromWishlist(@PathVariable Long watchId) {
    wishlistService.remove(SecurityUtils.currentUserId(), watchId);
    return ResponseEntity.noContent().build();
  }
}
