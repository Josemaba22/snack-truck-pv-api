package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.request.ProductAddonRequest;
import com.josemaba.marquesitasapi.dto.response.ProductAddonResponse;
import com.josemaba.marquesitasapi.entity.ProductAddon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductAddonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "available", source = "available", defaultValue = "true")
    ProductAddon toEntity(ProductAddonRequest request);

    ProductAddonResponse toResponse(ProductAddon addon);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "available", source = "available", defaultValue = "true")
    void updateEntityFromRequest(ProductAddonRequest request, @MappingTarget ProductAddon addon);
}
