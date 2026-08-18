package com.chronoshop.auth.controller;

import com.chronoshop.auth.security.SecurityUtils;
import com.chronoshop.auth.service.AddressService;
import com.chronoshop.auth.service.UserService;
import com.chronoshop.dto.UserDtos.AddressRequest;
import com.chronoshop.dto.UserDtos.AddressResponse;
import com.chronoshop.dto.UserDtos.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Nalog ulogovanog korisnika: profil i adrese za isporuku.
 * Lista zelja je premestena u order-service (WishlistItem zivi u orderdb).
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserService userService;
    private final AddressService addressService;

    public AccountController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.getById(SecurityUtils.currentUserId());
    }

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
}
