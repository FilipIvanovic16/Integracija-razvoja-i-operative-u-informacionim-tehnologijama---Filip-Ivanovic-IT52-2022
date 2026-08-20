package com.chronoshop.order.service;

import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.domain.enums.Role;
import com.chronoshop.dto.OrderDtos.CreateOrderRequest;
import com.chronoshop.dto.OrderDtos.OrderItemRequest;
import com.chronoshop.dto.UserDtos.UserResponse;
import com.chronoshop.dto.WatchDtos.WatchResponse;
import com.chronoshop.exception.BadRequestException;
import com.chronoshop.exception.InsufficientStockException;
import com.chronoshop.order.client.AuthClient;
import com.chronoshop.order.client.CatalogClient;
import com.chronoshop.order.domain.Order;
import com.chronoshop.order.event.OrderEventPublisher;
import com.chronoshop.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AuthClient authClient;
    @Mock
    private CatalogClient catalogClient;
    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderService orderService;

    private static final UserResponse CUSTOMER = new UserResponse(
            1L, "Petar", "Petrović", "kupac@chronoshop.rs", Role.CUSTOMER, true, LocalDateTime.now());

    @Test
    void createOrder_throwsInsufficientStock_whenRequestedExceedsAvailable() {
        when(authClient.getUser(1L)).thenReturn(CUSTOMER);
        when(catalogClient.getWatch(10L)).thenReturn(watch(10L, "Submariner", "SUB-1", "12000.00", 2, true));

        CreateOrderRequest req = new CreateOrderRequest(
                List.of(new OrderItemRequest(10L, 5)),
                null, "Ulica 1", "Novi Sad", "21000", "Srbija");

        assertThatThrownBy(() -> orderService.createOrder(1L, req))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Submariner");

        verify(catalogClient, never()).adjustStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_throwsBadRequest_whenWatchInactive() {
        when(authClient.getUser(1L)).thenReturn(CUSTOMER);
        when(catalogClient.getWatch(10L)).thenReturn(watch(10L, "Submariner", "SUB-1", "12000.00", 5, false));

        CreateOrderRequest req = new CreateOrderRequest(
                List.of(new OrderItemRequest(10L, 1)),
                null, "Ulica 1", "Novi Sad", "21000", "Srbija");

        assertThatThrownBy(() -> orderService.createOrder(1L, req))
                .isInstanceOf(BadRequestException.class);

        verify(catalogClient, never()).adjustStock(anyLong(), anyInt());
    }

    @Test
    void createOrder_reservesStockAndSavesOrder_whenStockSufficient() {
        when(authClient.getUser(1L)).thenReturn(CUSTOMER);
        when(catalogClient.getWatch(10L)).thenReturn(watch(10L, "Submariner", "SUB-1", "12000.00", 5, true));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest req = new CreateOrderRequest(
                List.of(new OrderItemRequest(10L, 2)),
                null, "Ulica 1", "Novi Sad", "21000", "Srbija");

        var response = orderService.createOrder(1L, req);

        verify(catalogClient).adjustStock(10L, -2);
        verify(orderEventPublisher).publishOrderCreated(any());
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalAmount()).isEqualByComparingTo("24000.00");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void createOrder_throwsBadRequest_whenNoItems() {
        when(authClient.getUser(1L)).thenReturn(CUSTOMER);

        CreateOrderRequest req = new CreateOrderRequest(List.of(), null, "Ulica 1", "Novi Sad", "21000", "Srbija");

        assertThatThrownBy(() -> orderService.createOrder(1L, req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateStatus_toCancelled_restoresStockForEachItem() {
        Order order = new Order();
        order.setId(5L);
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(1L);
        order.setCustomerEmail(CUSTOMER.email());
        order.setCustomerName("Petar Petrović");
        order.addItem(new com.chronoshop.order.domain.OrderItem(10L, "Submariner", "SUB-1", 2, new BigDecimal("12000.00")));
        when(orderRepository.findById(5L)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateStatus(5L, OrderStatus.CANCELLED);

        verify(catalogClient).adjustStock(10L, 2);
    }

    private WatchResponse watch(Long id, String name, String ref, String price, int stock, boolean active) {
        return new WatchResponse(id, name, ref, null, null, null, new BigDecimal(price), stock,
                stock > 0, null, null, null, null, null, List.of(), active, null, null, null);
    }
}
