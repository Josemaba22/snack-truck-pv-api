package com.josemaba.marquesitasapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record OrderItemRequest(

        @NotNull(message = "productId is required")
        UUID productId,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        Integer quantity,

        List<UUID> selectedAddonIds) {
}
