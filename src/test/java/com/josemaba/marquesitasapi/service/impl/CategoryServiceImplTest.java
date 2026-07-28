package com.josemaba.marquesitasapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.josemaba.marquesitasapi.dto.request.CategoryRequest;
import com.josemaba.marquesitasapi.dto.response.CategoryResponse;
import com.josemaba.marquesitasapi.entity.Category;
import com.josemaba.marquesitasapi.exception.BusinessRuleViolationException;
import com.josemaba.marquesitasapi.exception.DuplicateResourceException;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.CategoryMapper;
import com.josemaba.marquesitasapi.repository.CategoryRepository;
import com.josemaba.marquesitasapi.repository.ProductRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void create_shouldSaveCategory_whenNameIsUnique() {
        CategoryRequest request = new CategoryRequest("Bebidas", "Bebidas frias", 1);
        Category entity = Category.builder().name("Bebidas").description("Bebidas frias").displayOrder(1).build();
        Category saved = Category.builder().id(UUID.randomUUID()).name("Bebidas").description("Bebidas frias").displayOrder(1).build();
        CategoryResponse response = new CategoryResponse(saved.getId(), "Bebidas", "Bebidas frias", 1);

        given(categoryRepository.existsByNameIgnoreCase("Bebidas")).willReturn(false);
        given(categoryMapper.toEntity(request)).willReturn(entity);
        given(categoryRepository.save(entity)).willReturn(saved);
        given(categoryMapper.toResponse(saved)).willReturn(response);

        CategoryResponse result = categoryService.create(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenNameAlreadyExists() {
        CategoryRequest request = new CategoryRequest("Bebidas", null, 1);
        given(categoryRepository.existsByNameIgnoreCase("Bebidas")).willReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void delete_shouldThrowBusinessRuleViolationException_whenCategoryHasProducts() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(id).name("Bebidas").displayOrder(1).build();
        given(categoryRepository.findById(id)).willReturn(Optional.of(category));
        given(productRepository.existsByCategoryId(id)).willReturn(true);

        assertThatThrownBy(() -> categoryService.delete(id))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_shouldRemoveCategory_whenNoProductsAssociated() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(id).name("Bebidas").displayOrder(1).build();
        given(categoryRepository.findById(id)).willReturn(Optional.of(category));
        given(productRepository.existsByCategoryId(id)).willReturn(false);

        categoryService.delete(id);

        verify(categoryRepository).delete(category);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
        UUID id = UUID.randomUUID();
        given(categoryRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldThrowDuplicateResourceException_whenNewNameBelongsToAnotherCategory() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(id).name("Bebidas").displayOrder(1).build();
        CategoryRequest request = new CategoryRequest("Postres", null, 2);
        given(categoryRepository.findById(id)).willReturn(Optional.of(category));
        given(categoryRepository.existsByNameIgnoreCaseAndIdNot("Postres", id)).willReturn(true);

        assertThatThrownBy(() -> categoryService.update(id, request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(categoryRepository, never()).save(any());
    }
}
