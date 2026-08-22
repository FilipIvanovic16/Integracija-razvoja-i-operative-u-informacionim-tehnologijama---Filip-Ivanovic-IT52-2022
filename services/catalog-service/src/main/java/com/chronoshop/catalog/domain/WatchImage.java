package com.chronoshop.catalog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "watch_images")
@Getter
@Setter
@NoArgsConstructor
public class WatchImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "watch_id", nullable = false)
  private Watch watch;

  @Column(nullable = false, length = 1000)
  private String url;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;
}
