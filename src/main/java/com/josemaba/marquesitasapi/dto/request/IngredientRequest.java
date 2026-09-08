package com.josemaba.marquesitasapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record IngredientRequest(

        @NotBlank(message = "name is required")
        @Size(max = 50, message = "name must be at most 50 characters")
        String name,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "price must be zero or positive")
        BigDecimal price,

        Boolean available) {
}
