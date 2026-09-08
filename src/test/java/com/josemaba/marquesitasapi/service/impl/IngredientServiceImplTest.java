package com.josemaba.marquesitasapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.josemaba.marquesitasapi.dto.request.IngredientRequest;
import com.josemaba.marquesitasapi.dto.response.IngredientResponse;
import com.josemaba.marquesitasapi.entity.Ingredient;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.IngredientMapper;
import com.josemaba.marquesitasapi.repository.IngredientRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngredientServiceImplTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientServiceImpl ingredientService;

    @Test
    void create_shouldSaveIngredient() {
        IngredientRequest request = new IngredientRequest("Queso extra", new BigDecimal("10.00"), true);
        Ingredient entity = Ingredient.builder().name("Queso extra").price(new BigDecimal("10.00")).available(true).build();
        Ingredient saved = Ingredient.builder().id(UUID.randomUUID()).name("Queso extra").price(new BigDecimal("10.00")).available(true).build();
        IngredientResponse response = new IngredientResponse(saved.getId(), "Queso extra", new BigDecimal("10.00"), true);

        given(ingredientMapper.toEntity(request)).willReturn(entity);
        given(ingredientRepository.save(entity)).willReturn(saved);
        given(ingredientMapper.toResponse(saved)).willReturn(response);

        IngredientResponse result = ingredientService.create(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenIngredientDoesNotExist() {
        UUID id = UUID.randomUUID();
        IngredientRequest request = new IngredientRequest("Tocino", BigDecimal.ONE, true);
        given(ingredientRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.update(id, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldRemoveIngredient_whenExists() {
        UUID id = UUID.randomUUID();
        Ingredient ingredient = Ingredient.builder().id(id).name("Tocino").price(BigDecimal.ONE).available(true).build();
        given(ingredientRepository.findById(id)).willReturn(Optional.of(ingredient));

        ingredientService.delete(id);

        verify(ingredientRepository).delete(ingredient);
    }
}
