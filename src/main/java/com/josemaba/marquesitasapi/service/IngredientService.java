package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.IngredientRequest;
import com.josemaba.marquesitasapi.dto.response.IngredientResponse;
import java.util.List;
import java.util.UUID;

public interface IngredientService {

    IngredientResponse create(IngredientRequest request);

    IngredientResponse update(UUID id, IngredientRequest request);

    void delete(UUID id);

    IngredientResponse getById(UUID id);

    List<IngredientResponse> getAll();
}
