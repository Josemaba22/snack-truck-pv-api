package com.josemaba.marquesitasapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.josemaba.marquesitasapi.dto.request.OrderItemRequest;
import com.josemaba.marquesitasapi.dto.request.OrderRequest;
import com.josemaba.marquesitasapi.dto.request.OrderStatusUpdateRequest;
import com.josemaba.marquesitasapi.entity.Order;
import com.josemaba.marquesitasapi.entity.OrderDetail;
import com.josemaba.marquesitasapi.entity.OrderStatus;
import com.josemaba.marquesitasapi.entity.PaymentMethod;
import com.josemaba.marquesitasapi.entity.Product;
import com.josemaba.marquesitasapi.entity.ProductAddon;
import com.josemaba.marquesitasapi.entity.ProductDetail;
import com.josemaba.marquesitasapi.entity.SelectedAddonSnapshot;
import com.josemaba.marquesitasapi.exception.BusinessRuleViolationException;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.OrderMapper;
import com.josemaba.marquesitasapi.repository.OrderRepository;
import com.josemaba.marquesitasapi.repository.ProductDetailRepository;
import com.josemaba.marquesitasapi.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductDetailRepository productDetailRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Test
    void create_shouldCalculateLineAndOrderTotals_withAddonsAndPriceOverride() {
        UUID productId = UUID.randomUUID();
        UUID addon1Id = UUID.randomUUID();
        UUID addon2Id = UUID.randomUUID();

        Product product = Product.builder().id(productId).name("Marquesita").price(new BigDecimal("45.00")).available(true).build();

        ProductAddon addon1 = ProductAddon.builder().id(addon1Id).name("Queso extra").price(new BigDecimal("10.00")).available(true).build();
        ProductDetail detail1 = ProductDetail.builder().product(product).addon(addon1).available(true).build();

        ProductAddon addon2 = ProductAddon.builder().id(addon2Id).name("Tocino").price(new BigDecimal("8.00")).available(true).build();
        ProductDetail detail2 = ProductDetail.builder().product(product).addon(addon2).available(true)
                .priceOverride(new BigDecimal("5.00")).build();

        OrderItemRequest item = new OrderItemRequest(productId, 2, List.of(addon1Id, addon2Id));
        OrderRequest request = new OrderRequest(List.of(item), "sin cebolla", PaymentMethod.CASH);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productDetailRepository.findByProductIdAndAddonId(productId, addon1Id)).willReturn(Optional.of(detail1));
        given(productDetailRepository.findByProductIdAndAddonId(productId, addon2Id)).willReturn(Optional.of(detail2));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        orderService.create(request);

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getSubtotal()).isEqualByComparingTo("120.00");
        assertThat(savedOrder.getTotal()).isEqualByComparingTo("120.00");
        assertThat(savedOrder.getOrderDetails()).hasSize(1);

        OrderDetail line = savedOrder.getOrderDetails().get(0);
        assertThat(line.getUnitPrice()).isEqualByComparingTo("45.00");
        assertThat(line.getSubtotal()).isEqualByComparingTo("120.00");
        assertThat(line.getOrder()).isEqualTo(savedOrder);
        assertThat(line.getSelectedAddons()).extracting(SelectedAddonSnapshot::price)
                .containsExactlyInAnyOrder(new BigDecimal("10.00"), new BigDecimal("5.00"));
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        OrderItemRequest item = new OrderItemRequest(productId, 1, null);
        OrderRequest request = new OrderRequest(List.of(item), null, PaymentMethod.CASH);
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenProductNotAvailable() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").price(BigDecimal.TEN).available(false).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, null);
        OrderRequest request = new OrderRequest(List.of(item), null, PaymentMethod.CASH);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenAddonNotAssignedToProduct() {
        UUID productId = UUID.randomUUID();
        UUID addonId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").price(BigDecimal.TEN).available(true).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, List.of(addonId));
        OrderRequest request = new OrderRequest(List.of(item), null, PaymentMethod.CASH);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productDetailRepository.findByProductIdAndAddonId(productId, addonId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenAddonNotAvailableForProduct() {
        UUID productId = UUID.randomUUID();
        UUID addonId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").price(BigDecimal.TEN).available(true).build();
        ProductAddon addon = ProductAddon.builder().id(addonId).name("Jalapenos").price(BigDecimal.ONE).available(true).build();
        ProductDetail detail = ProductDetail.builder().product(product).addon(addon).available(false).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, List.of(addonId));
        OrderRequest request = new OrderRequest(List.of(item), null, PaymentMethod.CASH);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productDetailRepository.findByProductIdAndAddonId(productId, addonId)).willReturn(Optional.of(detail));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void updateStatus_shouldTransitionOrder_whenTransitionIsAllowed() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.PENDING).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        orderService.updateStatus(id, new OrderStatusUpdateRequest(OrderStatus.IN_PROGRESS));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_shouldSetCompletedAt_whenTransitioningToCompleted() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.READY).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        orderService.updateStatus(id, new OrderStatusUpdateRequest(OrderStatus.COMPLETED));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getCompletedAt()).isNotNull();
    }

    @Test
    void updateStatus_shouldThrowBusinessRuleViolationException_whenTransitionNotAllowed() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.PENDING).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(id, new OrderStatusUpdateRequest(OrderStatus.COMPLETED)))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancel_shouldSetStatusCancelled_whenOrderIsPending() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.PENDING).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        orderService.cancel(id);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_shouldThrowBusinessRuleViolationException_whenOrderAlreadyCompleted() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.COMPLETED).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(id))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(orderRepository, never()).save(any());
    }
}
