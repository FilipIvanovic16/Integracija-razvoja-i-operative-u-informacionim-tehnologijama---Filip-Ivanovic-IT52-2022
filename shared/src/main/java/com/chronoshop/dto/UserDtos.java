package com.chronoshop.dto;

import com.chronoshop.domain.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            Role role,
            boolean enabled,
            LocalDateTime createdAt
    ) {
    }

    public record UpdateRoleRequest(
            @NotNull Role role
    ) {
    }

    public record AddressRequest(
            @Size(max = 60) String label,
            @NotBlank @Size(max = 200) String street,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 20) String postalCode,
            @NotBlank @Size(max = 80) String country,
            @Size(max = 30) String phone
    ) {
    }

    public record AddressResponse(
            Long id,
            String label,
            String street,
            String city,
            String postalCode,
            String country,
            String phone
    ) {
    }
}
