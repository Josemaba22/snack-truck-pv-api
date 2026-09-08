package com.josemaba.marquesitasapi.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        List<OrderDetailIngredientResponse> ingredients,
        BigDecimal subtotal) {
}
