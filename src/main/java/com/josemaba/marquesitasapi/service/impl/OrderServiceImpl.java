package com.josemaba.marquesitasapi.service.impl;

import com.josemaba.marquesitasapi.dto.request.OrderItemRequest;
import com.josemaba.marquesitasapi.dto.request.OrderRequest;
import com.josemaba.marquesitasapi.dto.request.OrderStatusUpdateRequest;
import com.josemaba.marquesitasapi.dto.response.OrderResponse;
import com.josemaba.marquesitasapi.entity.Order;
import com.josemaba.marquesitasapi.entity.OrderDetail;
import com.josemaba.marquesitasapi.entity.OrderDetailAddon;
import com.josemaba.marquesitasapi.entity.OrderStatus;
import com.josemaba.marquesitasapi.entity.Product;
import com.josemaba.marquesitasapi.entity.ProductAddon;
import com.josemaba.marquesitasapi.exception.BusinessRuleViolationException;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.OrderMapper;
import com.josemaba.marquesitasapi.repository.OrderRepository;
import com.josemaba.marquesitasapi.repository.ProductAddonRepository;
import com.josemaba.marquesitasapi.repository.ProductDetailRepository;
import com.josemaba.marquesitasapi.repository.ProductRepository;
import com.josemaba.marquesitasapi.service.OrderService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED),
            OrderStatus.IN_PROGRESS, Set.of(OrderStatus.READY, OrderStatus.CANCELLED),
            OrderStatus.READY, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of());

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductAddonRepository productAddonRepository;
    private final ProductDetailRepository productDetailRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        List<OrderDetail> details = new ArrayList<>();
        BigDecimal orderSubtotal = BigDecimal.ZERO;

        for (OrderItemRequest item : request.items()) {
            OrderDetail detail = buildOrderDetail(item);
            details.add(detail);
            orderSubtotal = orderSubtotal.add(detail.getSubtotal());
        }

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .subtotal(orderSubtotal)
                .total(orderSubtotal)
                .notes(request.notes())
                .paymentMethod(request.paymentMethod())
                .orderDetails(details)
                .build();
        details.forEach(detail -> detail.setOrder(order));

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        return orderMapper.toResponse(findOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatusUpdateRequest request) {
        Order order = findOrder(id);
        applyStatusTransition(order, request.status());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID id) {
        Order order = findOrder(id);
        applyStatusTransition(order, OrderStatus.CANCELLED);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private OrderDetail buildOrderDetail(OrderItemRequest item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> ResourceNotFoundException.of("Product", item.productId()));
        if (!Boolean.TRUE.equals(product.getAvailable())) {
            throw new BusinessRuleViolationException("Product is not available: " + product.getId());
        }

        List<UUID> addonIds = item.selectedAddonIds() == null ? List.of() : item.selectedAddonIds();
        List<OrderDetailAddon> orderDetailAddons = new ArrayList<>();
        BigDecimal addonsSum = BigDecimal.ZERO;

        for (UUID addonId : addonIds) {
            ProductAddon addon = productAddonRepository.findById(addonId)
                    .orElseThrow(() -> ResourceNotFoundException.of("ProductAddon", addonId));
            if (!Boolean.TRUE.equals(addon.getAvailable())) {
                throw new BusinessRuleViolationException("Addon is not available: " + addonId);
            }
            if (!productDetailRepository.existsByProductIdAndAddonId(product.getId(), addonId)) {
                throw new BusinessRuleViolationException(
                        "Addon %s is not assigned to product %s".formatted(addonId, product.getId()));
            }
            addonsSum = addonsSum.add(addon.getPrice());
            orderDetailAddons.add(OrderDetailAddon.builder()
                    .addon(addon)
                    .addonName(addon.getName())
                    .unitPrice(addon.getPrice())
                    .build());
        }

        BigDecimal quantity = BigDecimal.valueOf(item.quantity());
        BigDecimal lineSubtotal = product.getPrice().add(addonsSum).multiply(quantity)
                .setScale(2, RoundingMode.HALF_UP);

        OrderDetail detail = OrderDetail.builder()
                .product(product)
                .quantity(item.quantity())
                .unitPrice(product.getPrice())
                .orderDetailAddons(orderDetailAddons)
                .subtotal(lineSubtotal)
                .build();
        orderDetailAddons.forEach(orderDetailAddon -> orderDetailAddon.setOrderDetail(detail));
        return detail;
    }

    private void applyStatusTransition(Order order, OrderStatus target) {
        OrderStatus current = order.getStatus();
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BusinessRuleViolationException(
                    "Cannot transition order from %s to %s".formatted(current, target));
        }
        order.setStatus(target);
        if (target == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", id));
    }
}
