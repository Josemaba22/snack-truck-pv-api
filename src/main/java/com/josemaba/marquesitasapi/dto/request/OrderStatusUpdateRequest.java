package com.josemaba.marquesitasapi.dto.request;

import java.time.LocalDateTime;
import com.josemaba.marquesitasapi.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull(message = "status is required")
        OrderStatus status,
        // Moment the change happened on the client; defaults to server time when absent (offline sync).
        LocalDateTime changedAt) {
}
