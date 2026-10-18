package com.josemaba.marquesitasapi.service;

import com.josemaba.marquesitasapi.dto.request.ProductRequest;
import com.josemaba.marquesitasapi.dto.response.ProductResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse update(UUID id, ProductRequest request);

    void delete(UUID id);

    ProductResponse getById(UUID id);

    List<ProductResponse> getAll();

    ProductResponse uploadImage(UUID id, MultipartFile file);

    ProductResponse deleteImage(UUID id);
}
