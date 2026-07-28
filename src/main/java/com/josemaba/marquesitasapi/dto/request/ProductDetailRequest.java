package com.josemaba.marquesitasapi.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProductDetailRequest(

        @NotNull(message = "addonId is required")
        UUID addonId) {
}
