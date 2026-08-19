package com.chronoshop.order.controller;

import com.chronoshop.dto.OrderDtos.CreateOrderRequest;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.order.security.SecurityUtils;
import com.chronoshop.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Porudžbine ulogovanog korisnika.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public PageResponse<OrderResponse> myOrders(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return orderService.listForUser(SecurityUtils.currentUserId(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOne(@PathVariable Long id) {
        return orderService.getForUser(SecurityUtils.currentUserId(), id, SecurityUtils.isAdmin());
    }
}
