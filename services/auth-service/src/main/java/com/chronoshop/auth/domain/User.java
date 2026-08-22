package com.chronoshop.auth.domain;

import com.chronoshop.domain.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String firstName;

  @Column(nullable = false, length = 80)
  private String lastName;

  @Column(nullable = false, length = 160)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role = Role.CUSTOMER;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  // Napomena: u monolitu je User imao @OneToMany i na Order i WishlistItem.
  // U database-per-service arhitekturi te tabele zive u orderdb (order-service),
  // pa ovde ostaje samo veza ka Address (koja takodje pripada auth-service/authdb).
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Address> addresses = new ArrayList<>();

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
