package com.josemaba.marquesitasapi.service.impl;

import com.josemaba.marquesitasapi.dto.request.ProductAddonRequest;
import com.josemaba.marquesitasapi.dto.response.ProductAddonResponse;
import com.josemaba.marquesitasapi.entity.ProductAddon;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.ProductAddonMapper;
import com.josemaba.marquesitasapi.repository.ProductAddonRepository;
import com.josemaba.marquesitasapi.service.ProductAddonService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductAddonServiceImpl implements ProductAddonService {

    private final ProductAddonRepository productAddonRepository;
    private final ProductAddonMapper productAddonMapper;

    @Override
    @Transactional
    public ProductAddonResponse create(ProductAddonRequest request) {
        ProductAddon addon = productAddonMapper.toEntity(request);
        ProductAddon saved = productAddonRepository.save(addon);
        return productAddonMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductAddonResponse update(UUID id, ProductAddonRequest request) {
        ProductAddon addon = findAddon(id);
        productAddonMapper.updateEntityFromRequest(request, addon);
        ProductAddon saved = productAddonRepository.save(addon);
        return productAddonMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ProductAddon addon = findAddon(id);
        productAddonRepository.delete(addon);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAddonResponse getById(UUID id) {
        return productAddonMapper.toResponse(findAddon(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAddonResponse> getAll() {
        return productAddonRepository.findAll().stream()
                .map(productAddonMapper::toResponse)
                .toList();
    }

    private ProductAddon findAddon(UUID id) {
        return productAddonRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductAddon", id));
    }
}
