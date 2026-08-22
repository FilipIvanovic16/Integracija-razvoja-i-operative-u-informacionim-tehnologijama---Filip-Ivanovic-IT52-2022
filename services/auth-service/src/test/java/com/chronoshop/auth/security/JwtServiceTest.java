package com.chronoshop.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  // 256-bitni test kljuc (Base64), nikad se ne koristi van testova
  private static final String TEST_SECRET =
      "Z2VuZXJpc2FuLXRham5pLWtsanVjLXphLWNocm9ub3Nob3AtMjAyNi1lb25pcw==";

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(TEST_SECRET, 60_000L);
  }

  @Test
  void generateToken_producesTokenWithCorrectEmailAndValidSignature() {
    String token = jwtService.generateToken(1L, "kupac@chronoshop.rs", "CUSTOMER");

    assertThat(token).isNotBlank();
    assertThat(jwtService.extractEmail(token)).isEqualTo("kupac@chronoshop.rs");
    assertThat(jwtService.isValid(token, "kupac@chronoshop.rs")).isTrue();
  }

  @Test
  void isValid_returnsFalseForDifferentEmail() {
    String token = jwtService.generateToken(1L, "kupac@chronoshop.rs", "CUSTOMER");

    assertThat(jwtService.isValid(token, "neko-drugi@chronoshop.rs")).isFalse();
  }

  @Test
  void isValid_returnsFalseForExpiredToken() throws InterruptedException {
    JwtService shortLived = new JwtService(TEST_SECRET, 1L);
    String token = shortLived.generateToken(1L, "kupac@chronoshop.rs", "CUSTOMER");

    Thread.sleep(20);

    assertThat(shortLived.isValid(token, "kupac@chronoshop.rs")).isFalse();
  }

  @Test
  void isValid_returnsFalseForMalformedToken() {
    assertThat(jwtService.isValid("ovo-nije-jwt", "kupac@chronoshop.rs")).isFalse();
  }
}
