package com.josemaba.marquesitasapi.service.impl;

import com.josemaba.marquesitasapi.dto.request.ProductRequest;
import com.josemaba.marquesitasapi.dto.response.ProductResponse;
import com.josemaba.marquesitasapi.entity.Category;
import com.josemaba.marquesitasapi.entity.Product;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.ProductMapper;
import com.josemaba.marquesitasapi.repository.CategoryRepository;
import com.josemaba.marquesitasapi.repository.ProductRepository;
import com.josemaba.marquesitasapi.service.ProductService;
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
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ImageStorage imageStorage;

    private static final String IMAGE_FOLDER = "products";

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        return create(request, null);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request, MultipartFile image) {
        Category category = findCategory(request.categoryId());
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        String newKey = hasImage(image) ? storeImage(image) : null;
        product.setImageKey(newKey);
        try {
            return productMapper.toResponse(productRepository.save(product));
        } catch (RuntimeException ex) {
            if (newKey != null) {
                imageStorage.delete(newKey);
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        return update(id, request, null);
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, ProductRequest request, MultipartFile image) {
        Product product = findProduct(id);
        Category category = findCategory(request.categoryId());
        productMapper.updateEntityFromRequest(request, product);
        product.setCategory(category);
        String oldKey = product.getImageKey();
        String newKey = hasImage(image) ? storeImage(image) : null;
        if (newKey != null) {
            product.setImageKey(newKey);
        }
        Product saved;
        try {
            saved = productRepository.save(product);
        } catch (RuntimeException ex) {
            if (newKey != null) {
                imageStorage.delete(newKey);
            }
            throw ex;
        }
        if (newKey != null && oldKey != null) {
            imageStorage.delete(oldKey);
        }
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = findProduct(id);
        String imageKey = product.getImageKey();
        productRepository.delete(product);
        if (imageKey != null) {
            imageStorage.delete(imageKey);
        }
    }

    @Override
    @Transactional
    public ProductResponse uploadImage(UUID id, MultipartFile file) {
        Product product = findProduct(id);
        String newKey = storeImage(file);
        String oldKey = product.getImageKey();
        product.setImageKey(newKey);
        Product saved = productRepository.save(product);
        if (oldKey != null) {
            imageStorage.delete(oldKey);
        }
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse deleteImage(UUID id) {
        Product product = findProduct(id);
        String oldKey = product.getImageKey();
        product.setImageKey(null);
        Product saved = productRepository.save(product);
        if (oldKey != null) {
            imageStorage.delete(oldKey);
        }
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        return productMapper.toResponse(findProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
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

    private Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }

    private Category findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
    }
}
