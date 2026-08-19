package com.chronoshop.order.domain;

import com.chronoshop.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(columnNames = "order_number"))
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    // Napomena: monolit je ovde imao @ManyToOne na User. Korisnik sada zivi u
    // authdb (auth-service), pa ostaje samo userId + snimljeni podaci u trenutku
    // poručivanja (customerEmail/customerName) - isto vazi za Payment (payment-service).
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 160)
    private String customerEmail;

    @Column(nullable = false, length = 160)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Snimljena adresa isporuke u trenutku poručivanja
    @Column(length = 200)
    private String shippingStreet;
    @Column(length = 100)
    private String shippingCity;
    @Column(length = 20)
    private String shippingPostalCode;
    @Column(length = 80)
    private String shippingCountry;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /** Ponovo izračunava ukupan iznos na osnovu stavki. */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
