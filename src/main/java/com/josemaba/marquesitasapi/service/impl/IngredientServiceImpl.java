package com.josemaba.marquesitasapi.service.impl;

import com.josemaba.marquesitasapi.dto.request.IngredientRequest;
import com.josemaba.marquesitasapi.dto.response.IngredientResponse;
import com.josemaba.marquesitasapi.entity.Ingredient;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.IngredientMapper;
import com.josemaba.marquesitasapi.repository.IngredientRepository;
import com.josemaba.marquesitasapi.service.IngredientService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    @Override
    @Transactional
    public IngredientResponse create(IngredientRequest request) {
        Ingredient ingredient = ingredientMapper.toEntity(request);
        Ingredient saved = ingredientRepository.save(ingredient);
        return ingredientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public IngredientResponse update(UUID id, IngredientRequest request) {
        Ingredient ingredient = findIngredient(id);
        ingredientMapper.updateEntityFromRequest(request, ingredient);
        Ingredient saved = ingredientRepository.save(ingredient);
        return ingredientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Ingredient ingredient = findIngredient(id);
        ingredientRepository.delete(ingredient);
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientResponse getById(UUID id) {
        return ingredientMapper.toResponse(findIngredient(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> getAll() {
        return ingredientRepository.findAll().stream()
                .map(ingredientMapper::toResponse)
                .toList();
    }

    private Ingredient findIngredient(UUID id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Ingredient", id));
    }
}
