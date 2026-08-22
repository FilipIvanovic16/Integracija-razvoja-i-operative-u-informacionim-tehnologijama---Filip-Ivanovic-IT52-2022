package com.chronoshop.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Adresa za isporuku koja pripada korisniku. */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(length = 60)
  private String label;

  @Column(nullable = false, length = 200)
  private String street;

  @Column(nullable = false, length = 100)
  private String city;

  @Column(nullable = false, length = 20)
  private String postalCode;

  @Column(nullable = false, length = 80)
  private String country;

  @Column(length = 30)
  private String phone;
}
