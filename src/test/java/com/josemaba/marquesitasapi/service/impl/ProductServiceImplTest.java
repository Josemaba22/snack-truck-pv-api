package com.josemaba.marquesitasapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.josemaba.marquesitasapi.dto.request.ProductRequest;
import com.josemaba.marquesitasapi.dto.response.ProductResponse;
import com.josemaba.marquesitasapi.entity.Category;
import com.josemaba.marquesitasapi.entity.Product;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.ProductMapper;
import com.josemaba.marquesitasapi.repository.CategoryRepository;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void create_shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();
        ProductRequest request = new ProductRequest("Marquesita", "desc", new BigDecimal("45.00"), categoryId, true);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_shouldAssignCategoryAndSaveProduct_whenCategoryExists() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Marquesitas").displayOrder(1).build();
        ProductRequest request = new ProductRequest("Marquesita", "desc", new BigDecimal("45.00"), categoryId, true);
        Product entity = Product.builder().name("Marquesita").description("desc").price(new BigDecimal("45.00")).available(true).build();
        Product saved = Product.builder().id(UUID.randomUUID()).name("Marquesita").category(category)
                .price(new BigDecimal("45.00")).available(true).build();
        ProductResponse response = new ProductResponse(saved.getId(), "Marquesita", "desc", new BigDecimal("45.00"),
                categoryId, "Marquesitas", true, null);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(productMapper.toEntity(request)).willReturn(entity);
        given(productRepository.save(entity)).willReturn(saved);
        given(productMapper.toResponse(saved)).willReturn(response);

        ProductResponse result = productService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(entity.getCategory()).isEqualTo(category);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        ProductRequest request = new ProductRequest("Marquesita", null, BigDecimal.TEN, categoryId, true);
        given(productRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(id, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).save(any());
    }
}
