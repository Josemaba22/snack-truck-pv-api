package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.response.OrderDetailResponse;
import com.josemaba.marquesitasapi.dto.response.SelectedAddonResponse;
import com.josemaba.marquesitasapi.entity.OrderDetail;
import com.josemaba.marquesitasapi.entity.SelectedAddonSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderDetailMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderDetailResponse toResponse(OrderDetail detail);

    SelectedAddonResponse toResponse(SelectedAddonSnapshot snapshot);
}
