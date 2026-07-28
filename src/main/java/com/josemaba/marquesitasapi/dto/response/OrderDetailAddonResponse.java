package com.josemaba.marquesitasapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderDetailAddonResponse(
        UUID id,
        UUID addonId,
        String addonName,
        BigDecimal unitPrice) {
}
