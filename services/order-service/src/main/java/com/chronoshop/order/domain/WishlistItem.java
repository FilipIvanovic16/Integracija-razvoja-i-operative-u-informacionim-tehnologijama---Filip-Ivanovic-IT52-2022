package com.chronoshop.order.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "wishlist_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "watch_id"}))
@Getter
@Setter
@NoArgsConstructor
public class WishlistItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // User i Watch zive u drugim bazama (authdb / catalogdb) - samo ID reference ovde.
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "watch_id", nullable = false)
  private Long watchId;

  @Column(nullable = false, updatable = false)
  private LocalDateTime addedAt = LocalDateTime.now();
}
