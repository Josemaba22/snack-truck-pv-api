package com.josemaba.marquesitasapi.dto.request;

import com.josemaba.marquesitasapi.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(

        @NotEmpty(message = "an order must contain at least one item")
        @Valid
        List<OrderItemRequest> items,

        String notes,

        @NotNull(message = "paymentMethod is required")
        PaymentMethod paymentMethod) {
}
