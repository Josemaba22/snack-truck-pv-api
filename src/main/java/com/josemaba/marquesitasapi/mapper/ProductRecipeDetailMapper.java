package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.request.ProductRecipeDetailRequest;
import com.josemaba.marquesitasapi.dto.response.ProductRecipeDetailResponse;
import com.josemaba.marquesitasapi.entity.ProductRecipeDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductRecipeDetailMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "isBase", source = "isBase", defaultValue = "true")
    ProductRecipeDetail toEntity(ProductRecipeDetailRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "ingredientPrice", source = "ingredient.price")
    ProductRecipeDetailResponse toResponse(ProductRecipeDetail detail);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "isBase", source = "isBase", defaultValue = "true")
    void updateEntityFromRequest(ProductRecipeDetailRequest request, @MappingTarget ProductRecipeDetail detail);
}
