package com.josemaba.marquesitasapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        UUID categoryId,
        String categoryName,
        Boolean available,
        LocalDateTime createdAt) {
}
