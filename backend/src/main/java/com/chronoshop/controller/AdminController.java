package com.chronoshop.controller;

import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.dto.OrderDtos.UpdateOrderStatusRequest;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.dto.UserDtos.UpdateRoleRequest;
import com.chronoshop.dto.UserDtos.UserResponse;
import com.chronoshop.service.OrderService;
import com.chronoshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Administratorski panel — upravljanje korisnicima i porudžbinama.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;

    public AdminController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    // ---------- Korisnici ----------

    @GetMapping("/users")
    public PageResponse<UserResponse> users(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 15, sort = "createdAt") Pageable pageable) {
        return userService.search(q, pageable);
    }

    @GetMapping("/users/{id}")
    public UserResponse user(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/users/{id}/role")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return userService.updateRole(id, request.role());
    }

    @PutMapping("/users/{id}/enabled")
    public UserResponse setEnabled(@PathVariable Long id, @RequestParam boolean value) {
        return userService.setEnabled(id, value);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Porudžbine ----------

    @GetMapping("/orders")
    public PageResponse<OrderResponse> orders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 15, sort = "createdAt") Pageable pageable) {
        return orderService.adminList(status, pageable);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse order(@PathVariable Long id) {
        return orderService.getForUser(null, id, true);
    }

    @PutMapping("/orders/{id}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }
}
