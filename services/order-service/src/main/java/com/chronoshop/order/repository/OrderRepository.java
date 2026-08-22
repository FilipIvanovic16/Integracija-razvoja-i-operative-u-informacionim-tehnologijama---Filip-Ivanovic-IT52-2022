package com.chronoshop.order.repository;

import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.order.domain.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Page<Order> findByUserId(Long userId, Pageable pageable);

  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  Optional<Order> findByOrderNumber(String orderNumber);
}
