package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.ProductAddonRequest;
import com.josemaba.marquesitasapi.dto.response.ProductAddonResponse;
import java.util.List;
import java.util.UUID;

public interface ProductAddonService {

    ProductAddonResponse create(ProductAddonRequest request);

    ProductAddonResponse update(UUID id, ProductAddonRequest request);

    void delete(UUID id);

    ProductAddonResponse getById(UUID id);

    List<ProductAddonResponse> getAll();
}
