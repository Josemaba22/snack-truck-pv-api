package com.josemaba.marquesitasapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductRecipeDetailServiceImplTest {

    @Mock
    private ProductRecipeDetailRepository productRecipeDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private ProductRecipeDetailMapper productRecipeDetailMapper;

    @InjectMocks
    private ProductRecipeDetailServiceImpl productRecipeDetailService;

    @Test
    void create_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        ProductRecipeDetailRequest request = new ProductRecipeDetailRequest(UUID.randomUUID(), true);
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productRecipeDetailService.create(productId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenIngredientNotAvailable() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").build();
        Ingredient ingredient = Ingredient.builder().id(ingredientId).name("Jalapenos").price(BigDecimal.ONE).available(false).build();
        ProductRecipeDetailRequest request = new ProductRecipeDetailRequest(ingredientId, false);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredientId)).willReturn(Optional.of(ingredient));

        assertThatThrownBy(() -> productRecipeDetailService.create(productId, request))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(productRecipeDetailRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenIngredientAlreadyAssignedToProduct() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").build();
        Ingredient ingredient = Ingredient.builder().id(ingredientId).name("Queso").price(BigDecimal.ONE).available(true).build();
        ProductRecipeDetailRequest request = new ProductRecipeDetailRequest(ingredientId, false);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredientId)).willReturn(Optional.of(ingredient));
        given(productRecipeDetailRepository.existsByProductIdAndIngredientId(productId, ingredientId)).willReturn(true);

        assertThatThrownBy(() -> productRecipeDetailService.create(productId, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_shouldSaveDetail_whenValid() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").build();
        Ingredient ingredient = Ingredient.builder().id(ingredientId).name("Queso").price(BigDecimal.ONE).available(true).build();
        ProductRecipeDetailRequest request = new ProductRecipeDetailRequest(ingredientId, false);
        ProductRecipeDetail entity = ProductRecipeDetail.builder().build();
        ProductRecipeDetail saved = ProductRecipeDetail.builder().id(UUID.randomUUID()).product(product).ingredient(ingredient).isBase(false).build();
        ProductRecipeDetailResponse response = new ProductRecipeDetailResponse(saved.getId(), productId, ingredientId, "Queso", BigDecimal.ONE, false);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(ingredientRepository.findById(ingredientId)).willReturn(Optional.of(ingredient));
        given(productRecipeDetailRepository.existsByProductIdAndIngredientId(productId, ingredientId)).willReturn(false);
        given(productRecipeDetailMapper.toEntity(request)).willReturn(entity);
        given(productRecipeDetailRepository.save(entity)).willReturn(saved);
        given(productRecipeDetailMapper.toResponse(saved)).willReturn(response);

        ProductRecipeDetailResponse result = productRecipeDetailService.create(productId, request);

        assertThat(result).isEqualTo(response);
        assertThat(entity.getProduct()).isEqualTo(product);
        assertThat(entity.getIngredient()).isEqualTo(ingredient);
    }
}
