package com.chronoshop.auth.mapper;

import com.chronoshop.auth.domain.Address;
import com.chronoshop.auth.domain.User;
import com.chronoshop.dto.UserDtos.AddressResponse;
import com.chronoshop.dto.UserDtos.UserResponse;

/** Mapiranje auth-service entiteta (User, Address) u DTO odgovore. */
public final class EntityMapper {

  private EntityMapper() {}

  public static UserResponse toUserResponse(User u) {
    return new UserResponse(
        u.getId(),
        u.getFirstName(),
        u.getLastName(),
        u.getEmail(),
        u.getRole(),
        u.isEnabled(),
        u.getCreatedAt());
  }

  public static AddressResponse toAddressResponse(Address a) {
    return new AddressResponse(
        a.getId(),
        a.getLabel(),
        a.getStreet(),
        a.getCity(),
        a.getPostalCode(),
        a.getCountry(),
        a.getPhone());
  }
}
