package com.josemaba.marquesitasapi.repository;

import com.josemaba.marquesitasapi.entity.Ingredient;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {
}
