package com.josemaba.marquesitasapi.dto.response;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        Integer displayOrder) {
}
