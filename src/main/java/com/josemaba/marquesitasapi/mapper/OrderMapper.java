package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.response.OrderResponse;
import com.josemaba.marquesitasapi.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = OrderDetailMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "items", source = "orderDetails")
    OrderResponse toResponse(Order order);
}
