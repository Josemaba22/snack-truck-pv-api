package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.request.IngredientRequest;
import com.josemaba.marquesitasapi.dto.response.IngredientResponse;
import com.josemaba.marquesitasapi.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IngredientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "available", source = "available", defaultValue = "true")
    Ingredient toEntity(IngredientRequest request);

    IngredientResponse toResponse(Ingredient ingredient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "available", source = "available", defaultValue = "true")
    void updateEntityFromRequest(IngredientRequest request, @MappingTarget Ingredient ingredient);
}
