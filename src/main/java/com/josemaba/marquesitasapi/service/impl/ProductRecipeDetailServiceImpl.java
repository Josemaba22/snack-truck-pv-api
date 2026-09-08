package com.josemaba.marquesitasapi.service.impl;

import com.josemaba.marquesitasapi.dto.request.ProductRecipeDetailRequest;
import com.josemaba.marquesitasapi.dto.response.ProductRecipeDetailResponse;
import com.josemaba.marquesitasapi.entity.Ingredient;
import com.josemaba.marquesitasapi.entity.Product;
import com.josemaba.marquesitasapi.entity.ProductRecipeDetail;
import com.josemaba.marquesitasapi.exception.BusinessRuleViolationException;
import com.josemaba.marquesitasapi.exception.DuplicateResourceException;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.ProductRecipeDetailMapper;
import com.josemaba.marquesitasapi.repository.IngredientRepository;
import com.josemaba.marquesitasapi.repository.ProductRecipeDetailRepository;
import com.josemaba.marquesitasapi.repository.ProductRepository;
import com.josemaba.marquesitasapi.service.ProductRecipeDetailService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductRecipeDetailServiceImpl implements ProductRecipeDetailService {

    private final ProductRecipeDetailRepository productRecipeDetailRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRecipeDetailMapper productRecipeDetailMapper;

    @Override
    @Transactional
    public ProductRecipeDetailResponse create(UUID productId, ProductRecipeDetailRequest request) {
        Product product = findProduct(productId);
        Ingredient ingredient = findAvailableIngredient(request.ingredientId());
        if (productRecipeDetailRepository.existsByProductIdAndIngredientId(productId, ingredient.getId())) {
            throw new DuplicateResourceException("Ingredient already assigned to this product: " + ingredient.getId());
        }
        ProductRecipeDetail detail = productRecipeDetailMapper.toEntity(request);
        detail.setProduct(product);
        detail.setIngredient(ingredient);
        ProductRecipeDetail saved = productRecipeDetailRepository.save(detail);
        return productRecipeDetailMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductRecipeDetailResponse update(UUID productId, UUID detailId, ProductRecipeDetailRequest request) {
        ProductRecipeDetail detail = findDetail(productId, detailId);
        Ingredient ingredient = findAvailableIngredient(request.ingredientId());
        boolean ingredientChanged = !detail.getIngredient().getId().equals(ingredient.getId());
        if (ingredientChanged && productRecipeDetailRepository.existsByProductIdAndIngredientId(productId, ingredient.getId())) {
            throw new DuplicateResourceException("Ingredient already assigned to this product: " + ingredient.getId());
        }
        productRecipeDetailMapper.updateEntityFromRequest(request, detail);
        detail.setIngredient(ingredient);
        ProductRecipeDetail saved = productRecipeDetailRepository.save(detail);
        return productRecipeDetailMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID productId, UUID detailId) {
        ProductRecipeDetail detail = findDetail(productId, detailId);
        productRecipeDetailRepository.delete(detail);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRecipeDetailResponse getById(UUID productId, UUID detailId) {
        return productRecipeDetailMapper.toResponse(findDetail(productId, detailId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRecipeDetailResponse> getAllByProduct(UUID productId) {
        findProduct(productId);
        return productRecipeDetailRepository.findByProductId(productId).stream()
                .map(productRecipeDetailMapper::toResponse)
                .toList();
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", productId));
    }

    private Ingredient findAvailableIngredient(UUID ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> ResourceNotFoundException.of("Ingredient", ingredientId));
        if (!Boolean.TRUE.equals(ingredient.getAvailable())) {
            throw new BusinessRuleViolationException("Ingredient is not available: " + ingredientId);
        }
        return ingredient;
    }

    private ProductRecipeDetail findDetail(UUID productId, UUID detailId) {
        ProductRecipeDetail detail = productRecipeDetailRepository.findById(detailId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductRecipeDetail", detailId));
        if (!detail.getProduct().getId().equals(productId)) {
            throw ResourceNotFoundException.of("ProductRecipeDetail", detailId);
        }
        return detail;
    }
}
