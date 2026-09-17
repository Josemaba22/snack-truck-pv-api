package com.josemaba.marquesitasapi.dto.response;

import com.josemaba.marquesitasapi.entity.OrderStatus;
import com.josemaba.marquesitasapi.entity.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        Long orderNumber,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal total,
        String notes,
        String customerName,
        PaymentMethod paymentMethod,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        List<OrderDetailResponse> items) {
}
