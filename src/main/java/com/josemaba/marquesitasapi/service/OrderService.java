package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.OrderRequest;
import com.josemaba.marquesitasapi.dto.request.OrderStatusUpdateRequest;
import com.josemaba.marquesitasapi.dto.response.OrderResponse;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    OrderResponse getById(UUID id);

    List<OrderResponse> getAll();

    OrderResponse updateStatus(UUID id, OrderStatusUpdateRequest request);

    OrderResponse cancel(UUID id);
}
