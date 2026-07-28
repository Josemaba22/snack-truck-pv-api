package com.josemaba.marquesitasapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductAddonResponse(
        UUID id,
        String name,
        BigDecimal price,
        Boolean available) {
}
