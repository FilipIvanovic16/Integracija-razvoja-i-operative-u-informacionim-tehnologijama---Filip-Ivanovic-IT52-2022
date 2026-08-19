package com.chronoshop.order.mapper;

import com.chronoshop.order.domain.Order;
import com.chronoshop.order.domain.OrderItem;
import com.chronoshop.order.domain.WishlistItem;
import com.chronoshop.dto.OrderDtos.OrderItemResponse;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.dto.WishlistDtos.WishlistItemResponse;

/**
 * Mapiranje order-service entiteta (Order, OrderItem, WishlistItem) u DTO odgovore.
 * Za razliku od monolita, Order/OrderItem/WishlistItem vise nemaju JPA relacije ka
 * User/Watch (druge baze), pa se koriste snimljena polja odnosno prosledjeni DTO
 * dobijen REST pozivom ka catalog-service (za WishlistItemResponse).
 */
public final class EntityMapper {

    private EntityMapper() {
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem i) {
        return new OrderItemResponse(
                i.getWatchId(),
                i.getWatchName(),
                i.getReferenceNumber(),
                i.getQuantity(),
                i.getUnitPrice(),
                i.getLineTotal()
        );
    }

    public static OrderResponse toOrderResponse(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getStatus(),
                o.getTotalAmount(),
                o.getCustomerEmail(),
                o.getCustomerName(),
                o.getShippingStreet(),
                o.getShippingCity(),
                o.getShippingPostalCode(),
                o.getShippingCountry(),
                o.getCreatedAt(),
                o.getItems().stream().map(EntityMapper::toOrderItemResponse).toList()
        );
    }

    public static WishlistItemResponse toWishlistItemResponse(WishlistItem item, WatchResponse watch) {
        return new WishlistItemResponse(item.getId(), watch, item.getAddedAt());
    }
}
