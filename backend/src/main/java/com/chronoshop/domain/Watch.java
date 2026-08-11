package com.chronoshop.domain;

import com.chronoshop.domain.enums.Documentation;
import com.chronoshop.domain.enums.Gender;
import com.chronoshop.domain.enums.MovementType;
import com.chronoshop.domain.enums.WatchCondition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@Table(name = "watches", uniqueConstraints = @UniqueConstraint(columnNames = "reference_number"))
@Getter
@Setter
@NoArgsConstructor
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "reference_number", nullable = false, length = 80)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MovementType movement;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    
    private Integer caseDiameterMm;

    
    private Integer waterResistanceM;

    @Column(length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "watch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("sortOrder ASC")
    private List<WatchImage> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "watch_condition", length = 20)
    private WatchCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Documentation documentation;

    @Column(length = 80)
    private String material;

    /** Da li je artikal aktivan (vidljiv u katalogu). */
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
