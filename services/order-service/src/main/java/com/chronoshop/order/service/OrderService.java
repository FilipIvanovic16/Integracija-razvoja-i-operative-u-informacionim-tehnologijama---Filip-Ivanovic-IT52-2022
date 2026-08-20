package com.chronoshop.order.service;

import com.chronoshop.dto.UserDtos.AddressResponse;
import com.chronoshop.dto.UserDtos.UserResponse;
import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.dto.OrderDtos.CreateOrderRequest;
import com.chronoshop.dto.OrderDtos.OrderItemRequest;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.event.OrderCreatedEvent;
import com.chronoshop.exception.BadRequestException;
import com.chronoshop.exception.InsufficientStockException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.order.client.AuthClient;
import com.chronoshop.order.client.StockClient;
import com.chronoshop.order.client.WatchStockInfo;
import com.chronoshop.order.domain.Order;
import com.chronoshop.order.domain.OrderItem;
import com.chronoshop.order.event.OrderEventPublisher;
import com.chronoshop.order.mapper.EntityMapper;
import com.chronoshop.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuthClient authClient;
    private final StockClient stockClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository, AuthClient authClient, StockClient stockClient,
                        OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.authClient = authClient;
        this.stockClient = stockClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest req) {
        UserResponse user = authClient.getUser(userId);
        if (req.items() == null || req.items().isEmpty()) {
            throw new BadRequestException("Porudžbina mora sadržati bar jednu stavku.");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setCustomerEmail(user.email());
        order.setCustomerName(user.firstName() + " " + user.lastName());
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        applyShipping(order, userId, req);

        // Prvo proveravamo dostupnost SVIH stavki (gRPC CheckStock, REST fallback pri
        // nedostupnosti), tek onda rezervišemo zalihe za svaku (gRPC ReserveStock) -
        // smanjuje (ali ne eliminiše, bez sage/kompenzacije) prozor za nekonzistentnost
        // između order-service i catalog-service dok su to dva odvojena poziva umesto
        // jedne lokalne transakcije kao u monolitu.
        List<WatchStockInfo> watches = new ArrayList<>();
        for (OrderItemRequest itemReq : req.items()) {
            WatchStockInfo watch = stockClient.checkStock(itemReq.watchId(), itemReq.quantity());
            if (!watch.active()) {
                throw new BadRequestException("Sat '" + watch.name() + "' trenutno nije dostupan za prodaju.");
            }
            if (itemReq.quantity() > watch.stockQuantity()) {
                throw new InsufficientStockException(watch.name(), itemReq.quantity(), watch.stockQuantity());
            }
            watches.add(watch);
        }

        for (int i = 0; i < req.items().size(); i++) {
            OrderItemRequest itemReq = req.items().get(i);
            WatchStockInfo watch = watches.get(i);
            stockClient.reserveStock(watch.watchId(), -itemReq.quantity());
            order.addItem(new OrderItem(watch.watchId(), watch.name(), watch.referenceNumber(),
                    itemReq.quantity(), watch.price()));
        }

        order.recalculateTotal();
        Order saved = orderRepository.save(order);

        orderEventPublisher.publishOrderCreated(OrderCreatedEvent.of(
                saved.getId(), saved.getOrderNumber(), saved.getCustomerEmail(),
                saved.getCustomerName(), saved.getTotalAmount(), "EUR"));

        return EntityMapper.toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listForUser(Long userId, Pageable pageable) {
        return PageResponse.from(orderRepository.findByUserId(userId, pageable), EntityMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getForUser(Long userId, Long orderId, boolean isAdmin) {
        Order order = findEntity(orderId);
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Porudžbina", orderId);
        }
        return EntityMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> adminList(OrderStatus status, Pageable pageable) {
        Page<Order> page = (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PageResponse.from(page, EntityMapper::toOrderResponse);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = findEntity(orderId);
        OrderStatus old = order.getStatus();

        // Otkazivanje vraća rezervisane zalihe nazad u catalog-service
        if (newStatus == OrderStatus.CANCELLED && old != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                stockClient.reserveStock(item.getWatchId(), item.getQuantity());
            }
        }
        order.setStatus(newStatus);
        return EntityMapper.toOrderResponse(orderRepository.save(order));
    }

    public Order findEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Porudžbina", id));
    }

    private void applyShipping(Order order, Long userId, CreateOrderRequest req) {
        if (req.addressId() != null) {
            AddressResponse addr = authClient.getAddress(userId, req.addressId());
            order.setShippingStreet(addr.street());
            order.setShippingCity(addr.city());
            order.setShippingPostalCode(addr.postalCode());
            order.setShippingCountry(addr.country());
        } else {
            if (req.shippingStreet() == null || req.shippingCity() == null
                    || req.shippingPostalCode() == null || req.shippingCountry() == null) {
                throw new BadRequestException("Potrebno je izabrati adresu ili uneti podatke za isporuku.");
            }
            order.setShippingStreet(req.shippingStreet());
            order.setShippingCity(req.shippingCity());
            order.setShippingPostalCode(req.shippingPostalCode());
            order.setShippingCountry(req.shippingCountry());
        }
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rand = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + datePart + "-" + rand;
    }
}
