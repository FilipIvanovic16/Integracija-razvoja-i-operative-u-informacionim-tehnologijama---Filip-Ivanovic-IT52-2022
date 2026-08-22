package com.chronoshop.order.controller;

import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.dto.OrderDtos.UpdateOrderStatusRequest;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/** Administratorski uvid u porudžbine (korisnički deo admin panela je u auth-service). */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

  private final OrderService orderService;

  public AdminOrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public PageResponse<OrderResponse> orders(
      @RequestParam(required = false) OrderStatus status,
      @PageableDefault(size = 15, sort = "createdAt") Pageable pageable) {
    return orderService.adminList(status, pageable);
  }

  @GetMapping("/{id}")
  public OrderResponse order(@PathVariable Long id) {
    return orderService.getForUser(null, id, true);
  }

  @PutMapping("/{id}/status")
  public OrderResponse updateOrderStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
    return orderService.updateStatus(id, request.status());
  }
}
