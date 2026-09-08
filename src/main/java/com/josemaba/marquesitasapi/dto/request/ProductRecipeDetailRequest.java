package com.josemaba.marquesitasapi.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProductRecipeDetailRequest(

        @NotNull(message = "ingredientId is required")
        UUID ingredientId,

        Boolean isBase) {
}
