package com.josemaba.marquesitasapi.service.impl;

import com.josemaba.marquesitasapi.dto.request.IngredientRequest;
import com.josemaba.marquesitasapi.dto.response.IngredientResponse;
import com.josemaba.marquesitasapi.entity.Ingredient;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.IngredientMapper;
import com.josemaba.marquesitasapi.repository.IngredientRepository;
import com.josemaba.marquesitasapi.service.IngredientService;
import com.josemaba.marquesitasapi.storage.ImageStorage;
import com.josemaba.marquesitasapi.util.ImageValidator;
import com.josemaba.marquesitasapi.util.ImageValidator.ValidatedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;
    private final ImageStorage imageStorage;

    private static final String IMAGE_FOLDER = "ingredients";

    @Override
    @Transactional
    public IngredientResponse create(IngredientRequest request) {
        return create(request, null);
    }

    @Override
    @Transactional
    public IngredientResponse create(IngredientRequest request, MultipartFile image) {
        Ingredient ingredient = ingredientMapper.toEntity(request);
        String newKey = hasImage(image) ? storeImage(image) : null;
        ingredient.setImageKey(newKey);
        try {
            return ingredientMapper.toResponse(ingredientRepository.save(ingredient));
        } catch (RuntimeException ex) {
            if (newKey != null) {
                imageStorage.delete(newKey);
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public IngredientResponse update(UUID id, IngredientRequest request) {
        return update(id, request, null);
    }

    @Override
    @Transactional
    public IngredientResponse update(UUID id, IngredientRequest request, MultipartFile image) {
        Ingredient ingredient = findIngredient(id);
        ingredientMapper.updateEntityFromRequest(request, ingredient);
        String oldKey = ingredient.getImageKey();
        String newKey = hasImage(image) ? storeImage(image) : null;
        if (newKey != null) {
            ingredient.setImageKey(newKey);
        }
        Ingredient saved;
        try {
            saved = ingredientRepository.save(ingredient);
        } catch (RuntimeException ex) {
            if (newKey != null) {
                imageStorage.delete(newKey);
            }
            throw ex;
        }
        if (newKey != null && oldKey != null) {
            imageStorage.delete(oldKey);
        }
        return ingredientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Ingredient ingredient = findIngredient(id);
        String imageKey = ingredient.getImageKey();
        ingredientRepository.delete(ingredient);
        if (imageKey != null) {
            imageStorage.delete(imageKey);
        }
    }

    @Override
    @Transactional
    public IngredientResponse uploadImage(UUID id, MultipartFile file) {
        Ingredient ingredient = findIngredient(id);
        String newKey = storeImage(file);
        String oldKey = ingredient.getImageKey();
        ingredient.setImageKey(newKey);
        Ingredient saved = ingredientRepository.save(ingredient);
        if (oldKey != null) {
            imageStorage.delete(oldKey);
        }
        return ingredientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public IngredientResponse deleteImage(UUID id) {
        Ingredient ingredient = findIngredient(id);
        String oldKey = ingredient.getImageKey();
        ingredient.setImageKey(null);
        Ingredient saved = ingredientRepository.save(ingredient);
        if (oldKey != null) {
            imageStorage.delete(oldKey);
        }
        return ingredientMapper.toResponse(saved);
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

    private boolean hasImage(MultipartFile image) {
        return image != null && !image.isEmpty();
    }

    private String storeImage(MultipartFile file) {
        ValidatedImage image = ImageValidator.validate(file);
        try (InputStream content = file.getInputStream()) {
            return imageStorage.store(IMAGE_FOLDER, image.extension(), image.contentType(), content, file.getSize());
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not store image", ex);
        }
    }

    private Ingredient findIngredient(UUID id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Ingredient", id));
    }
}
