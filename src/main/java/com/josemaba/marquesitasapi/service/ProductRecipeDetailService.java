package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.ProductRecipeDetailRequest;
import com.josemaba.marquesitasapi.dto.response.ProductRecipeDetailResponse;
import java.util.List;
import java.util.UUID;

public interface ProductRecipeDetailService {

    ProductRecipeDetailResponse create(UUID productId, ProductRecipeDetailRequest request);

    ProductRecipeDetailResponse update(UUID productId, UUID detailId, ProductRecipeDetailRequest request);

    void delete(UUID productId, UUID detailId);

    ProductRecipeDetailResponse getById(UUID productId, UUID detailId);

    List<ProductRecipeDetailResponse> getAllByProduct(UUID productId);
}
