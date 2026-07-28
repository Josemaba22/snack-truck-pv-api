package com.josemaba.marquesitasapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        UUID productId,
        UUID addonId,
        String addonName,
        BigDecimal addonPrice) {
}
