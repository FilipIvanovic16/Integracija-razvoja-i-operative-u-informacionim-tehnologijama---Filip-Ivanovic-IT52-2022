package com.chronoshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

  private AuthDtos() {}

  public record RegisterRequest(
      @NotBlank @Size(max = 80) String firstName,
      @NotBlank @Size(max = 80) String lastName,
      @NotBlank @Email @Size(max = 160) String email,
      @NotBlank @Size(min = 6, max = 100) String password) {}

  public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

  public record AuthResponse(
      String token, String tokenType, Long userId, String email, String fullName, String role) {}
}
