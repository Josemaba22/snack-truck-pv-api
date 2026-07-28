package com.josemaba.marquesitasapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "name is required")
        @Size(max = 50, message = "name must be at most 50 characters")
        String name,

        String description,

        @NotNull(message = "displayOrder is required")
        @PositiveOrZero(message = "displayOrder must be zero or positive")
        Integer displayOrder) {
}
