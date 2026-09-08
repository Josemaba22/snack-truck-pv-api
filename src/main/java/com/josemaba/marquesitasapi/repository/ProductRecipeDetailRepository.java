package com.josemaba.marquesitasapi.repository;

import com.josemaba.marquesitasapi.entity.ProductRecipeDetail;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRecipeDetailRepository extends JpaRepository<ProductRecipeDetail, UUID> {

    boolean existsByProductIdAndIngredientId(UUID productId, UUID ingredientId);

    boolean existsByProductIdAndIngredientIdAndIsBase(UUID productId, UUID ingredientId, Boolean isBase);

    List<ProductRecipeDetail> findByProductId(UUID productId);
}
