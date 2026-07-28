package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.response.OrderDetailAddonResponse;
import com.josemaba.marquesitasapi.dto.response.OrderDetailResponse;
import com.josemaba.marquesitasapi.entity.OrderDetail;
import com.josemaba.marquesitasapi.entity.OrderDetailAddon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderDetailMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "addons", source = "orderDetailAddons")
    OrderDetailResponse toResponse(OrderDetail detail);

    @Mapping(target = "addonId", source = "addon.id")
    OrderDetailAddonResponse toResponse(OrderDetailAddon orderDetailAddon);
}
