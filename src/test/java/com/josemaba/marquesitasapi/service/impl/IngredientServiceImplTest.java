package com.josemaba.marquesitasapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.josemaba.marquesitasapi.dto.request.IngredientRequest;
import com.josemaba.marquesitasapi.dto.response.IngredientResponse;
import com.josemaba.marquesitasapi.entity.Ingredient;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.IngredientMapper;
import com.josemaba.marquesitasapi.exception.InvalidImageException;
import com.josemaba.marquesitasapi.repository.IngredientRepository;
import com.josemaba.marquesitasapi.storage.ImageStorage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class IngredientServiceImplTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @Mock
    private ImageStorage imageStorage;

    @InjectMocks
    private IngredientServiceImpl ingredientService;

    @Test
    void create_shouldSaveIngredient() {
        IngredientRequest request = new IngredientRequest("Queso extra", new BigDecimal("10.00"), true);
        Ingredient entity = Ingredient.builder().name("Queso extra").price(new BigDecimal("10.00")).available(true).build();
        Ingredient saved = Ingredient.builder().id(UUID.randomUUID()).name("Queso extra").price(new BigDecimal("10.00")).available(true).build();
        IngredientResponse response = new IngredientResponse(saved.getId(), "Queso extra", new BigDecimal("10.00"), true, null);

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

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Test
    void uploadImage_shouldStoreNewImageAndDeleteOldOne_whenIngredientHasImage() throws IOException {
        UUID id = UUID.randomUUID();
        Ingredient ingredient = Ingredient.builder().id(id).name("Fresa").imageKey("ingredients/old.png").build();
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", PNG);
        given(ingredientRepository.findById(id)).willReturn(Optional.of(ingredient));
        given(imageStorage.store(ArgumentMatchers.eq("ingredients"), ArgumentMatchers.eq("png"),
                ArgumentMatchers.eq("image/png"), any(InputStream.class), ArgumentMatchers.anyLong()))
                .willReturn("ingredients/new.png");
        given(ingredientRepository.save(ingredient)).willReturn(ingredient);

        ingredientService.uploadImage(id, file);

        assertThat(ingredient.getImageKey()).isEqualTo("ingredients/new.png");
        verify(imageStorage).delete("ingredients/old.png");
    }

    @Test
    void uploadImage_shouldThrowInvalidImageException_whenFileIsNotAnImage() {
        UUID id = UUID.randomUUID();
        Ingredient ingredient = Ingredient.builder().id(id).name("Fresa").build();
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", "not an image!".getBytes());
        given(ingredientRepository.findById(id)).willReturn(Optional.of(ingredient));

        assertThatThrownBy(() -> ingredientService.uploadImage(id, file))
                .isInstanceOf(InvalidImageException.class);
        verify(imageStorage, never()).delete(any());
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    void deleteImage_shouldClearKeyAndDeleteFromStorage_whenIngredientHasImage() throws IOException {
        UUID id = UUID.randomUUID();
        Ingredient ingredient = Ingredient.builder().id(id).name("Fresa").imageKey("ingredients/old.png").build();
        given(ingredientRepository.findById(id)).willReturn(Optional.of(ingredient));
        given(ingredientRepository.save(ingredient)).willReturn(ingredient);

        ingredientService.deleteImage(id);

        assertThat(ingredient.getImageKey()).isNull();
        verify(imageStorage).delete("ingredients/old.png");
    }
}
