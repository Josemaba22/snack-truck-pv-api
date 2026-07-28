package com.josemaba.marquesitasapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record SelectedAddonResponse(
        UUID addonId,
        String name,
        BigDecimal price) {
}
