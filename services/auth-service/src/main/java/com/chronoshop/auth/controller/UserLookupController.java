package com.chronoshop.auth.controller;

import com.chronoshop.auth.service.AddressService;
import com.chronoshop.auth.service.UserService;
import com.chronoshop.dto.UserDtos.AddressResponse;
import com.chronoshop.dto.UserDtos.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Interni lookup za ostale servise (npr. order-service validira korisnika i razresava adresu
 * isporuke pre kreiranja porudzbine). Nema JWT ovde - security/* ostaje samo u auth-service, a
 * gateway (PR feat/api-gateway) ce kasnije ograniciti ko sme da pogodi ove rute spolja. Podaci koji
 * se vracaju nisu osetljivi (bez lozinke).
 */
@RestController
@RequestMapping("/api/users")
public class UserLookupController {

  private final UserService userService;
  private final AddressService addressService;

  public UserLookupController(UserService userService, AddressService addressService) {
    this.userService = userService;
    this.addressService = addressService;
  }

  @GetMapping("/{id}")
  public UserResponse getById(@PathVariable Long id) {
    return userService.getById(id);
  }

  @GetMapping("/{userId}/addresses/{addressId}")
  public AddressResponse getAddress(@PathVariable Long userId, @PathVariable Long addressId) {
    return addressService.getForUser(userId, addressId);
  }
}
