package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.request.ProductDetailRequest;
import com.josemaba.marquesitasapi.dto.response.ProductDetailResponse;
import com.josemaba.marquesitasapi.entity.ProductDetail;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductDetailMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "addon", ignore = true)
    @Mapping(target = "available", source = "available", defaultValue = "true")
    ProductDetail toEntity(ProductDetailRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "addonId", source = "addon.id")
    @Mapping(target = "addonName", source = "addon.name")
    @Mapping(target = "addonPrice", source = "addon.price")
    @Mapping(target = "effectivePrice", expression = "java(resolveEffectivePrice(detail))")
    ProductDetailResponse toResponse(ProductDetail detail);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "addon", ignore = true)
    @Mapping(target = "available", source = "available", defaultValue = "true")
    void updateEntityFromRequest(ProductDetailRequest request, @MappingTarget ProductDetail detail);

    default BigDecimal resolveEffectivePrice(ProductDetail detail) {
        if (detail.getPriceOverride() != null) {
            return detail.getPriceOverride();
        }
        return detail.getAddon() != null ? detail.getAddon().getPrice() : null;
    }
}
