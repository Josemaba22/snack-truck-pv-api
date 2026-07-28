package com.josemaba.marquesitasapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductDetailRequest(

        @NotNull(message = "addonId is required")
        UUID addonId,

        @DecimalMin(value = "0.0", inclusive = true, message = "priceOverride must be zero or positive")
        BigDecimal priceOverride,

        Boolean available) {
}
