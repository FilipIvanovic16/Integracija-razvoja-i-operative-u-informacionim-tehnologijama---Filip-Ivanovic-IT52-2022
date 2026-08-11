package com.chronoshop.service;

import com.chronoshop.domain.*;
import com.chronoshop.domain.enums.OrderStatus;
import com.chronoshop.dto.OrderDtos.CreateOrderRequest;
import com.chronoshop.dto.OrderDtos.OrderItemRequest;
import com.chronoshop.dto.OrderDtos.OrderResponse;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.exception.BadRequestException;
import com.chronoshop.exception.InsufficientStockException;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.mapper.EntityMapper;
import com.chronoshop.repository.AddressRepository;
import com.chronoshop.repository.OrderRepository;
import com.chronoshop.repository.UserRepository;
import com.chronoshop.repository.WatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final WatchRepository watchRepository;
    private final AddressRepository addressRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        WatchRepository watchRepository, AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.watchRepository = watchRepository;
        this.addressRepository = addressRepository;
    }

    
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik", userId));

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        applyShipping(order, user, req);

        for (OrderItemRequest itemReq : req.items()) {
            Watch watch = watchRepository.findById(itemReq.watchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sat", itemReq.watchId()));

            if (!watch.isActive()) {
                throw new BadRequestException("Sat '" + watch.getName() + "' trenutno nije dostupan za prodaju.");
            }
            int requested = itemReq.quantity();
            if (requested > watch.getStockQuantity()) {
                throw new InsufficientStockException(watch.getName(), requested, watch.getStockQuantity());
            }
            // Umanjenje zaliha
            watch.setStockQuantity(watch.getStockQuantity() - requested);
            watchRepository.save(watch);

            OrderItem item = new OrderItem(watch, requested, watch.getPrice());
            order.addItem(item);
        }

        order.recalculateTotal();
        if (order.getItems().isEmpty()) {
            throw new BadRequestException("Porudžbina mora sadržati bar jednu stavku.");
        }
        return EntityMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listForUser(Long userId, Pageable pageable) {
        return PageResponse.from(orderRepository.findByUserId(userId, pageable), EntityMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getForUser(Long userId, Long orderId, boolean isAdmin) {
        Order order = findEntity(orderId);
        if (!isAdmin && !order.getUser().getId().equals(userId)) {
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

        // Ako se porudžbina otkazuje, vraćamo količine na stanje
        if (newStatus == OrderStatus.CANCELLED && old != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Watch w = item.getWatch();
                w.setStockQuantity(w.getStockQuantity() + item.getQuantity());
                watchRepository.save(w);
            }
        }
        order.setStatus(newStatus);
        return EntityMapper.toOrderResponse(orderRepository.save(order));
    }

    public Order findEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Porudžbina", id));
    }

    private void applyShipping(Order order, User user, CreateOrderRequest req) {
        if (req.addressId() != null) {
            Address addr = addressRepository.findByIdAndUserId(req.addressId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Adresa", req.addressId()));
            order.setShippingStreet(addr.getStreet());
            order.setShippingCity(addr.getCity());
            order.setShippingPostalCode(addr.getPostalCode());
            order.setShippingCountry(addr.getCountry());
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
