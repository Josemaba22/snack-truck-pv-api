package com.josemaba.marquesitasapi.dto.request;

import com.josemaba.marquesitasapi.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull(message = "status is required")
        OrderStatus status) {
}
