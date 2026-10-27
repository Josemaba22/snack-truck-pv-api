package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.IngredientRequest;
import com.josemaba.marquesitasapi.dto.response.IngredientResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface IngredientService {

    IngredientResponse create(IngredientRequest request);

    IngredientResponse update(UUID id, IngredientRequest request);

    IngredientResponse create(IngredientRequest request, MultipartFile image);

    IngredientResponse update(UUID id, IngredientRequest request, MultipartFile image);

    void delete(UUID id);

    IngredientResponse getById(UUID id);

    List<IngredientResponse> getAll();

    IngredientResponse uploadImage(UUID id, MultipartFile file);

    IngredientResponse deleteImage(UUID id);
}
