package com.josemaba.marquesitasapi.dto.response;

import com.josemaba.marquesitasapi.entity.IngredientAction;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderDetailIngredientResponse(
        UUID id,
        UUID ingredientId,
        String ingredientName,
        BigDecimal unitPrice,
        IngredientAction action) {
}
