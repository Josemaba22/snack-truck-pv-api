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
import com.josemaba.marquesitasapi.entity.Ingredient;
import com.josemaba.marquesitasapi.entity.IngredientAction;
import com.josemaba.marquesitasapi.entity.Order;
import com.josemaba.marquesitasapi.entity.OrderDetail;
import com.josemaba.marquesitasapi.entity.OrderDetailIngredient;
import com.josemaba.marquesitasapi.entity.OrderStatus;
import com.josemaba.marquesitasapi.entity.PaymentMethod;
import com.josemaba.marquesitasapi.entity.Product;
import com.josemaba.marquesitasapi.exception.BusinessRuleViolationException;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.OrderMapper;
import com.josemaba.marquesitasapi.repository.IngredientRepository;
import com.josemaba.marquesitasapi.repository.OrderRepository;
import com.josemaba.marquesitasapi.repository.ProductRecipeDetailRepository;
import com.josemaba.marquesitasapi.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private IngredientRepository ingredientRepository;

    @Mock
    private ProductRecipeDetailRepository productRecipeDetailRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Test
    void create_shouldCalculateLineAndOrderTotals_withExtraIngredients() {
        UUID productId = UUID.randomUUID();
        UUID ingredient1Id = UUID.randomUUID();
        UUID ingredient2Id = UUID.randomUUID();

        Product product = Product.builder().id(productId).name("Marquesita").price(new BigDecimal("45.00")).available(true).build();
        Ingredient ingredient1 = Ingredient.builder().id(ingredient1Id).name("Queso extra").price(new BigDecimal("10.00")).available(true).build();
        Ingredient ingredient2 = Ingredient.builder().id(ingredient2Id).name("Tocino").price(new BigDecimal("8.00")).available(true).build();

        OrderItemRequest item = new OrderItemRequest(productId, 2, List.of(ingredient1Id, ingredient2Id), null);
        OrderRequest request = new OrderRequest(List.of(item), "sin cebolla", null, PaymentMethod.CASH, null, null);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredient1Id)).willReturn(Optional.of(ingredient1));
        given(ingredientRepository.findById(ingredient2Id)).willReturn(Optional.of(ingredient2));
        given(productRecipeDetailRepository.existsByProductIdAndIngredientIdAndIsBase(productId, ingredient1Id, false)).willReturn(true);
        given(productRecipeDetailRepository.existsByProductIdAndIngredientIdAndIsBase(productId, ingredient2Id, false)).willReturn(true);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        orderService.create(request);

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getSubtotal()).isEqualByComparingTo("126.00");
        assertThat(savedOrder.getTotal()).isEqualByComparingTo("126.00");
        assertThat(savedOrder.getOrderDetails()).hasSize(1);

        OrderDetail line = savedOrder.getOrderDetails().get(0);
        assertThat(line.getUnitPrice()).isEqualByComparingTo("45.00");
        assertThat(line.getSubtotal()).isEqualByComparingTo("126.00");
        assertThat(line.getOrder()).isEqualTo(savedOrder);
        assertThat(line.getOrderDetailIngredients()).extracting(OrderDetailIngredient::getUnitPrice)
                .containsExactlyInAnyOrder(new BigDecimal("10.00"), new BigDecimal("8.00"));
        assertThat(line.getOrderDetailIngredients()).allSatisfy(ingredient -> {
            assertThat(ingredient.getOrderDetail()).isEqualTo(line);
            assertThat(ingredient.getAction()).isEqualTo(IngredientAction.ADDED);
        });
    }

    @Test
    void create_shouldApplyRemovedIngredient_withoutAffectingSubtotal() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();

        Product product = Product.builder().id(productId).name("Marquesita").price(new BigDecimal("45.00")).available(true).build();
        Ingredient ingredient = Ingredient.builder().id(ingredientId).name("Nutella").price(new BigDecimal("20.00")).available(true).build();

        OrderItemRequest item = new OrderItemRequest(productId, 1, null, List.of(ingredientId));
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, null, null);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredientId)).willReturn(Optional.of(ingredient));
        given(productRecipeDetailRepository.existsByProductIdAndIngredientIdAndIsBase(productId, ingredientId, true)).willReturn(true);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        orderService.create(request);

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getSubtotal()).isEqualByComparingTo("45.00");
        OrderDetail line = savedOrder.getOrderDetails().get(0);
        assertThat(line.getOrderDetailIngredients()).hasSize(1);
        OrderDetailIngredient removed = line.getOrderDetailIngredients().get(0);
        assertThat(removed.getAction()).isEqualTo(IngredientAction.REMOVED);
        assertThat(removed.getUnitPrice()).isEqualByComparingTo("0.00");
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        OrderItemRequest item = new OrderItemRequest(productId, 1, null, null);
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, null, null);
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenProductNotAvailable() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").price(BigDecimal.TEN).available(false).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, null, null);
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, null, null);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenIngredientDoesNotExist() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").price(BigDecimal.TEN).available(true).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, List.of(ingredientId), null);
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, null, null);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredientId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenIngredientNotAvailable() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").price(BigDecimal.TEN).available(true).build();
        Ingredient ingredient = Ingredient.builder().id(ingredientId).name("Jalapenos").price(BigDecimal.ONE).available(false).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, List.of(ingredientId), null);
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, null, null);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredientId)).willReturn(Optional.of(ingredient));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenIngredientNotAvailableAsExtraForProduct() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").price(BigDecimal.TEN).available(true).build();
        Ingredient ingredient = Ingredient.builder().id(ingredientId).name("Jalapenos").price(BigDecimal.ONE).available(true).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, List.of(ingredientId), null);
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, null, null);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredientId)).willReturn(Optional.of(ingredient));
        given(productRecipeDetailRepository.existsByProductIdAndIngredientIdAndIsBase(productId, ingredientId, false)).willReturn(false);

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenRemovedIngredientNotPartOfBaseRecipe() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").price(BigDecimal.TEN).available(true).build();
        Ingredient ingredient = Ingredient.builder().id(ingredientId).name("Nutella").price(BigDecimal.ONE).available(true).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, null, List.of(ingredientId));
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, null, null);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredientId)).willReturn(Optional.of(ingredient));
        given(productRecipeDetailRepository.existsByProductIdAndIngredientIdAndIsBase(productId, ingredientId, true)).willReturn(false);

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void updateStatus_shouldTransitionOrder_whenTransitionIsAllowed() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.PENDING).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        orderService.updateStatus(id, new OrderStatusUpdateRequest(OrderStatus.IN_PROGRESS, null));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_shouldSetCompletedAt_whenTransitioningToCompleted() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.READY).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        orderService.updateStatus(id, new OrderStatusUpdateRequest(OrderStatus.COMPLETED, null));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getCompletedAt()).isNotNull();
    }

    @Test
    void create_shouldReturnExistingOrder_whenClientOrderIdAlreadyExists() {
        UUID clientOrderId = UUID.randomUUID();
        Order existing = Order.builder().id(UUID.randomUUID()).clientOrderId(clientOrderId).status(OrderStatus.PENDING).build();
        OrderItemRequest item = new OrderItemRequest(UUID.randomUUID(), 1, null, null);
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, clientOrderId, null);
        given(orderRepository.findByClientOrderId(clientOrderId)).willReturn(Optional.of(existing));

        orderService.create(request);

        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).findById(any());
    }

    @Test
    void create_shouldUseClientCreatedAtAndClientOrderId_whenProvided() {
        UUID productId = UUID.randomUUID();
        UUID clientOrderId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 18, 14, 30);
        Product product = Product.builder().id(productId).name("Marquesita").price(new BigDecimal("45.00")).available(true).build();
        OrderItemRequest item = new OrderItemRequest(productId, 1, null, null);
        OrderRequest request = new OrderRequest(List.of(item), null, null, PaymentMethod.CASH, clientOrderId, createdAt);
        given(orderRepository.findByClientOrderId(clientOrderId)).willReturn(Optional.empty());
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        orderService.create(request);

        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getCreatedAt()).isEqualTo(createdAt);
        assertThat(orderCaptor.getValue().getClientOrderId()).isEqualTo(clientOrderId);
    }

    @Test
    void updateStatus_shouldUseChangedAtAsCompletedAt_whenProvided() {
        UUID id = UUID.randomUUID();
        LocalDateTime changedAt = LocalDateTime.of(2026, 10, 18, 15, 0);
        Order order = Order.builder().id(id).status(OrderStatus.READY).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        orderService.updateStatus(id, new OrderStatusUpdateRequest(OrderStatus.COMPLETED, changedAt));

        assertThat(order.getCompletedAt()).isEqualTo(changedAt);
    }

    @Test
    void updateStatus_shouldRevertCompletedToReadyAndClearCompletedAt() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.COMPLETED).completedAt(LocalDateTime.now()).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        orderService.updateStatus(id, new OrderStatusUpdateRequest(OrderStatus.READY, null));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY);
        assertThat(order.getCompletedAt()).isNull();
    }

    @Test
    void updateStatus_shouldThrowBusinessRuleViolationException_whenTransitionNotAllowed() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).status(OrderStatus.PENDING).build();
        given(orderRepository.findById(id)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(id, new OrderStatusUpdateRequest(OrderStatus.COMPLETED, null)))
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
