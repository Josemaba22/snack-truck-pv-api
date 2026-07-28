package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.ProductDetailRequest;
import com.josemaba.marquesitasapi.dto.response.ProductDetailResponse;
import java.util.List;
import java.util.UUID;

public interface ProductDetailService {

    ProductDetailResponse create(UUID productId, ProductDetailRequest request);

    ProductDetailResponse update(UUID productId, UUID detailId, ProductDetailRequest request);

    void delete(UUID productId, UUID detailId);

    ProductDetailResponse getById(UUID productId, UUID detailId);

    List<ProductDetailResponse> getAllByProduct(UUID productId);
}
