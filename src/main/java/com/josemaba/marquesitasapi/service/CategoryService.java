package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.CategoryRequest;
import com.josemaba.marquesitasapi.dto.response.CategoryResponse;
import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(UUID id, CategoryRequest request);

    void delete(UUID id);

    CategoryResponse getById(UUID id);

    List<CategoryResponse> getAll();
}
