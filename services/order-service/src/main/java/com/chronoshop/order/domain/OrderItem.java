package com.chronoshop.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Watch zivi u catalogdb (catalog-service) - ovde ostaje samo watchId +
    // snimljeni naziv/referenca/cena u trenutku poručivanja (istorija porudzbine
    // se ne sme promeniti ako se kasnije izmeni katalog).
    @Column(name = "watch_id", nullable = false)
    private Long watchId;

    @Column(nullable = false, length = 150)
    private String watchName;

    @Column(name = "reference_number", nullable = false, length = 80)
    private String referenceNumber;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    public OrderItem(Long watchId, String watchName, String referenceNumber, Integer quantity, BigDecimal unitPrice) {
        this.watchId = watchId;
        this.watchName = watchName;
        this.referenceNumber = referenceNumber;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
