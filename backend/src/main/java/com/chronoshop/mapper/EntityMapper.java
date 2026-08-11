package com.chronoshop.mapper;

import com.chronoshop.domain.*;

import java.util.List;
import com.chronoshop.dto.CatalogDtos.BrandResponse;
import com.chronoshop.dto.CatalogDtos.CategoryResponse;
import com.chronoshop.dto.OrderDtos.OrderItemResponse;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.dto.PaymentDtos.PaymentResponse;
import com.chronoshop.dto.UserDtos.AddressResponse;
import com.chronoshop.dto.UserDtos.UserResponse;
import com.chronoshop.dto.WatchDtos.RefName;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.dto.WishlistDtos.WishlistItemResponse;

/**
 * Centralizovano mapiranje entiteta u DTO odgovore.
 */
public final class EntityMapper {

    private EntityMapper() {
    }

    public static BrandResponse toBrandResponse(Brand b) {
        return new BrandResponse(b.getId(), b.getName(), b.getCountry(), b.getDescription(), b.getLogoUrl());
    }

    public static CategoryResponse toCategoryResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }

    public static WatchResponse toWatchResponse(Watch w) {
        List<String> imageUrls = w.getImages().stream()
                .map(WatchImage::getUrl)
                .toList();
        String imageUrl = imageUrls.isEmpty() ? w.getImageUrl() : imageUrls.get(0);
        return new WatchResponse(
                w.getId(),
                w.getName(),
                w.getReferenceNumber(),
                new RefName(w.getBrand().getId(), w.getBrand().getName()),
                new RefName(w.getCategory().getId(), w.getCategory().getName()),
                w.getDescription(),
                w.getPrice(),
                w.getStockQuantity(),
                w.getStockQuantity() != null && w.getStockQuantity() > 0,
                w.getMovement(),
                w.getGender(),
                w.getCaseDiameterMm(),
                w.getWaterResistanceM(),
                imageUrl,
                imageUrls,
                w.isActive(),
                w.getCondition(),
                w.getDocumentation(),
                w.getMaterial()
        );
    }

    public static UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getRole(), u.isEnabled(), u.getCreatedAt());
    }

    public static AddressResponse toAddressResponse(Address a) {
        return new AddressResponse(a.getId(), a.getLabel(), a.getStreet(), a.getCity(),
                a.getPostalCode(), a.getCountry(), a.getPhone());
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem i) {
        return new OrderItemResponse(
                i.getWatch().getId(),
                i.getWatch().getName(),
                i.getWatch().getReferenceNumber(),
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
                o.getUser().getEmail(),
                o.getUser().getFullName(),
                o.getShippingStreet(),
                o.getShippingCity(),
                o.getShippingPostalCode(),
                o.getShippingCountry(),
                o.getCreatedAt(),
                o.getItems().stream().map(EntityMapper::toOrderItemResponse).toList()
        );
    }

    public static PaymentResponse toPaymentResponse(Payment p) {
        Order o = p.getOrder();
        return new PaymentResponse(
                p.getId(),
                o.getOrderNumber(),
                p.getStripePaymentIntentId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getCustomerEmail() != null ? p.getCustomerEmail() : o.getUser().getEmail(),
                o.getUser().getFullName(),
                p.getPaidAt(),
                p.getCreatedAt()
        );
    }

    public static WishlistItemResponse toWishlistItemResponse(WishlistItem item) {
        return new WishlistItemResponse(item.getId(), toWatchResponse(item.getWatch()), item.getAddedAt());
    }
}
