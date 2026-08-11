package com.chronoshop.dto;

import com.chronoshop.domain.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderItemRequest(
            @NotNull Long watchId,
            @NotNull @Min(1) Integer quantity
    ) {
    }

    public record CreateOrderRequest(
            @NotEmpty @Valid List<OrderItemRequest> items,
            
            Long addressId,
            @Size(max = 200) String shippingStreet,
            @Size(max = 100) String shippingCity,
            @Size(max = 20) String shippingPostalCode,
            @Size(max = 80) String shippingCountry
    ) {
    }

    public record OrderItemResponse(
            Long watchId,
            String watchName,
            String referenceNumber,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }

    public record OrderResponse(
            Long id,
            String orderNumber,
            OrderStatus status,
            BigDecimal totalAmount,
            String customerEmail,
            String customerName,
            String shippingStreet,
            String shippingCity,
            String shippingPostalCode,
            String shippingCountry,
            LocalDateTime createdAt,
            List<OrderItemResponse> items
    ) {
    }

    public record UpdateOrderStatusRequest(
            @NotNull OrderStatus status
    ) {
    }
}
