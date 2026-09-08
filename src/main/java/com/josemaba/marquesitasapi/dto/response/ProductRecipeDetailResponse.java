package com.josemaba.marquesitasapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRecipeDetailResponse(
        UUID id,
        UUID productId,
        UUID ingredientId,
        String ingredientName,
        BigDecimal ingredientPrice,
        Boolean isBase) {
}
