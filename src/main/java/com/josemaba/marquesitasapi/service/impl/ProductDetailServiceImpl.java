package com.josemaba.marquesitasapi.service.impl;

import com.josemaba.marquesitasapi.dto.request.ProductDetailRequest;
import com.josemaba.marquesitasapi.dto.response.ProductDetailResponse;
import com.josemaba.marquesitasapi.entity.Product;
import com.josemaba.marquesitasapi.entity.ProductAddon;
import com.josemaba.marquesitasapi.entity.ProductDetail;
import com.josemaba.marquesitasapi.exception.BusinessRuleViolationException;
import com.josemaba.marquesitasapi.exception.DuplicateResourceException;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.ProductDetailMapper;
import com.josemaba.marquesitasapi.repository.ProductAddonRepository;
import com.josemaba.marquesitasapi.repository.ProductDetailRepository;
import com.josemaba.marquesitasapi.repository.ProductRepository;
import com.josemaba.marquesitasapi.service.ProductDetailService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductDetailRepository productDetailRepository;
    private final ProductRepository productRepository;
    private final ProductAddonRepository productAddonRepository;
    private final ProductDetailMapper productDetailMapper;

    @Override
    @Transactional
    public ProductDetailResponse create(UUID productId, ProductDetailRequest request) {
        Product product = findProduct(productId);
        ProductAddon addon = findAvailableAddon(request.addonId());
        if (productDetailRepository.existsByProductIdAndAddonId(productId, addon.getId())) {
            throw new DuplicateResourceException("Addon already assigned to this product: " + addon.getId());
        }
        ProductDetail detail = productDetailMapper.toEntity(request);
        detail.setProduct(product);
        detail.setAddon(addon);
        ProductDetail saved = productDetailRepository.save(detail);
        return productDetailMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse update(UUID productId, UUID detailId, ProductDetailRequest request) {
        ProductDetail detail = findDetail(productId, detailId);
        ProductAddon addon = findAvailableAddon(request.addonId());
        boolean addonChanged = !detail.getAddon().getId().equals(addon.getId());
        if (addonChanged && productDetailRepository.existsByProductIdAndAddonId(productId, addon.getId())) {
            throw new DuplicateResourceException("Addon already assigned to this product: " + addon.getId());
        }
        productDetailMapper.updateEntityFromRequest(request, detail);
        detail.setAddon(addon);
        ProductDetail saved = productDetailRepository.save(detail);
        return productDetailMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID productId, UUID detailId) {
        ProductDetail detail = findDetail(productId, detailId);
        productDetailRepository.delete(detail);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getById(UUID productId, UUID detailId) {
        return productDetailMapper.toResponse(findDetail(productId, detailId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailResponse> getAllByProduct(UUID productId) {
        findProduct(productId);
        return productDetailRepository.findByProductId(productId).stream()
                .map(productDetailMapper::toResponse)
                .toList();
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", productId));
    }

    private ProductAddon findAvailableAddon(UUID addonId) {
        ProductAddon addon = productAddonRepository.findById(addonId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductAddon", addonId));
        if (!Boolean.TRUE.equals(addon.getAvailable())) {
            throw new BusinessRuleViolationException("Addon is not available: " + addonId);
        }
        return addon;
    }

    private ProductDetail findDetail(UUID productId, UUID detailId) {
        ProductDetail detail = productDetailRepository.findById(detailId)
                .orElseThrow(() -> ResourceNotFoundException.of("ProductDetail", detailId));
        if (!detail.getProduct().getId().equals(productId)) {
            throw ResourceNotFoundException.of("ProductDetail", detailId);
        }
        return detail;
    }
}
