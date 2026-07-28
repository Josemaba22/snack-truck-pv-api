package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.ProductRequest;
import com.josemaba.marquesitasapi.dto.response.ProductResponse;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse update(UUID id, ProductRequest request);

    void delete(UUID id);

    ProductResponse getById(UUID id);

    List<ProductResponse> getAll();
}
