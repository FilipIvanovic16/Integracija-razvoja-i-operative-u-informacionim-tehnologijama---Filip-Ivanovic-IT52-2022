package com.chronoshop.controller;

import com.chronoshop.dto.UserDtos.AddressRequest;
import com.chronoshop.dto.UserDtos.AddressResponse;
import com.chronoshop.dto.UserDtos.UserResponse;
import com.chronoshop.dto.WishlistDtos.AddWishlistRequest;
import com.chronoshop.dto.WishlistDtos.WishlistItemResponse;
import com.chronoshop.security.SecurityUtils;
import com.chronoshop.service.AddressService;
import com.chronoshop.service.UserService;
import com.chronoshop.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Nalog ulogovanog korisnika: profil, adrese za isporuku i lista želja.
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserService userService;
    private final AddressService addressService;
    private final WishlistService wishlistService;

    public AccountController(UserService userService, AddressService addressService, WishlistService wishlistService) {
        this.userService = userService;
        this.addressService = addressService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.getById(SecurityUtils.currentUserId());
    }

    // ---------- Adrese ----------

    @GetMapping("/addresses")
    public List<AddressResponse> addresses() {
        return addressService.listForUser(SecurityUtils.currentUserId());
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(@Valid @RequestBody AddressRequest request) {
        AddressResponse res = addressService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/addresses/{id}")
    public AddressResponse updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return addressService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.delete(SecurityUtils.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Lista želja ----------

    @GetMapping("/wishlist")
    public List<WishlistItemResponse> wishlist() {
        return wishlistService.listForUser(SecurityUtils.currentUserId());
    }

    @PostMapping("/wishlist")
    public ResponseEntity<WishlistItemResponse> addToWishlist(@Valid @RequestBody AddWishlistRequest request) {
        WishlistItemResponse res = wishlistService.add(SecurityUtils.currentUserId(), request.watchId());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @DeleteMapping("/wishlist/{watchId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long watchId) {
        wishlistService.remove(SecurityUtils.currentUserId(), watchId);
        return ResponseEntity.noContent().build();
    }
}
