package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.response.OrderDetailIngredientResponse;
import com.josemaba.marquesitasapi.dto.response.OrderDetailResponse;
import com.josemaba.marquesitasapi.entity.OrderDetail;
import com.josemaba.marquesitasapi.entity.OrderDetailIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderDetailMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "ingredients", source = "orderDetailIngredients")
    OrderDetailResponse toResponse(OrderDetail detail);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    OrderDetailIngredientResponse toResponse(OrderDetailIngredient orderDetailIngredient);
}
