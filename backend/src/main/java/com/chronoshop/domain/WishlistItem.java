package com.chronoshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "wishlist_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "watch_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "watch_id", nullable = false)
    private Watch watch;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt = LocalDateTime.now();
}
