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
import com.josemaba.marquesitasapi.storage.ImageStorage;
import com.josemaba.marquesitasapi.exception.InvalidImageException;
import java.io.IOException;
import java.io.InputStream;
import org.mockito.ArgumentMatchers;
import org.springframework.mock.web.MockMultipartFile;
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

    @Mock
    private ImageStorage imageStorage;

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
                categoryId, "Marquesitas", true, null, null);

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

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Test
    void uploadImage_shouldStoreNewImageAndDeleteOldOne_whenProductHasImage() throws IOException {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).name("Marquesita").imageKey("products/old.png").build();
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", PNG);
        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(imageStorage.store(ArgumentMatchers.eq("products"), ArgumentMatchers.eq("png"),
                ArgumentMatchers.eq("image/png"), any(InputStream.class), ArgumentMatchers.anyLong()))
                .willReturn("products/new.png");
        given(productRepository.save(product)).willReturn(product);

        productService.uploadImage(id, file);

        assertThat(product.getImageKey()).isEqualTo("products/new.png");
        verify(imageStorage).delete("products/old.png");
    }

    @Test
    void uploadImage_shouldThrowInvalidImageException_whenFileIsNotAnImage() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).name("Marquesita").build();
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", "not an image!".getBytes());
        given(productRepository.findById(id)).willReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.uploadImage(id, file))
                .isInstanceOf(InvalidImageException.class);
        verify(imageStorage, never()).delete(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteImage_shouldClearKeyAndDeleteFromStorage_whenProductHasImage() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).name("Marquesita").imageKey("products/old.png").build();
        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);

        productService.deleteImage(id);

        assertThat(product.getImageKey()).isNull();
        verify(imageStorage).delete("products/old.png");
    }

    private void givenImageStoredAs(String key) throws IOException {
        given(imageStorage.store(ArgumentMatchers.eq("products"), ArgumentMatchers.eq("png"),
                ArgumentMatchers.eq("image/png"), any(InputStream.class), ArgumentMatchers.anyLong()))
                .willReturn(key);
    }

    @Test
    void createWithImage_shouldStoreImageAndSetKey_whenImageProvided() throws IOException {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Marquesitas").displayOrder(1).build();
        ProductRequest request = new ProductRequest("Marquesita", "desc", new BigDecimal("45.00"), categoryId, true);
        Product entity = Product.builder().name("Marquesita").build();
        MockMultipartFile image = new MockMultipartFile("image", "x.png", "image/png", PNG);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(productMapper.toEntity(request)).willReturn(entity);
        givenImageStoredAs("products/new.png");
        given(productRepository.save(entity)).willReturn(entity);

        productService.create(request, image);

        assertThat(entity.getImageKey()).isEqualTo("products/new.png");
        assertThat(entity.getCategory()).isEqualTo(category);
    }

    @Test
    void createWithImage_shouldNotTouchStorage_whenImageIsNullOrEmpty() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Marquesitas").displayOrder(1).build();
        ProductRequest request = new ProductRequest("Marquesita", "desc", new BigDecimal("45.00"), categoryId, true);
        Product entity = Product.builder().name("Marquesita").build();
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(productMapper.toEntity(request)).willReturn(entity);
        given(productRepository.save(entity)).willReturn(entity);

        productService.create(request, new MockMultipartFile("image", new byte[0]));

        assertThat(entity.getImageKey()).isNull();
        verify(imageStorage, never()).delete(any());
    }

    @Test
    void createWithImage_shouldThrowInvalidImageException_whenFileIsNotAnImage() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Marquesitas").displayOrder(1).build();
        ProductRequest request = new ProductRequest("Marquesita", "desc", new BigDecimal("45.00"), categoryId, true);
        Product entity = Product.builder().name("Marquesita").build();
        MockMultipartFile image = new MockMultipartFile("image", "x.png", "image/png", "not an image!".getBytes());
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(productMapper.toEntity(request)).willReturn(entity);

        assertThatThrownBy(() -> productService.create(request, image))
                .isInstanceOf(InvalidImageException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createWithImage_shouldDeleteStoredImage_whenSaveFails() throws IOException {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Marquesitas").displayOrder(1).build();
        ProductRequest request = new ProductRequest("Marquesita", "desc", new BigDecimal("45.00"), categoryId, true);
        Product entity = Product.builder().name("Marquesita").build();
        MockMultipartFile image = new MockMultipartFile("image", "x.png", "image/png", PNG);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(productMapper.toEntity(request)).willReturn(entity);
        givenImageStoredAs("products/new.png");
        given(productRepository.save(entity)).willThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> productService.create(request, image))
                .isInstanceOf(IllegalStateException.class);
        verify(imageStorage).delete("products/new.png");
    }

    @Test
    void updateWithImage_shouldReplaceImageAndDeleteOldOne_whenImageProvided() throws IOException {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Marquesitas").displayOrder(1).build();
        Product product = Product.builder().id(id).name("Marquesita").imageKey("products/old.png").build();
        ProductRequest request = new ProductRequest("Marquesita", null, BigDecimal.TEN, categoryId, true);
        MockMultipartFile image = new MockMultipartFile("image", "x.png", "image/png", PNG);
        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        givenImageStoredAs("products/new.png");
        given(productRepository.save(product)).willReturn(product);

        productService.update(id, request, image);

        assertThat(product.getImageKey()).isEqualTo("products/new.png");
        verify(imageStorage).delete("products/old.png");
    }

    @Test
    void updateWithImage_shouldKeepExistingImage_whenNoImageProvided() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Marquesitas").displayOrder(1).build();
        Product product = Product.builder().id(id).name("Marquesita").imageKey("products/old.png").build();
        ProductRequest request = new ProductRequest("Marquesita", null, BigDecimal.TEN, categoryId, true);
        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(productRepository.save(product)).willReturn(product);

        productService.update(id, request, null);

        assertThat(product.getImageKey()).isEqualTo("products/old.png");
        verify(imageStorage, never()).delete(any());
    }

    @Test
    void updateWithImage_shouldDeleteNewImageAndKeepOld_whenSaveFails() throws IOException {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Marquesitas").displayOrder(1).build();
        Product product = Product.builder().id(id).name("Marquesita").imageKey("products/old.png").build();
        ProductRequest request = new ProductRequest("Marquesita", null, BigDecimal.TEN, categoryId, true);
        MockMultipartFile image = new MockMultipartFile("image", "x.png", "image/png", PNG);
        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        givenImageStoredAs("products/new.png");
        given(productRepository.save(product)).willThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> productService.update(id, request, image))
                .isInstanceOf(IllegalStateException.class);
        verify(imageStorage).delete("products/new.png");
        verify(imageStorage, never()).delete("products/old.png");
    }
}
