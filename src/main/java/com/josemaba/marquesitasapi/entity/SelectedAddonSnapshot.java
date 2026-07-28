package com.josemaba.marquesitasapi.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record SelectedAddonSnapshot(UUID addonId, String name, BigDecimal price) {
}
