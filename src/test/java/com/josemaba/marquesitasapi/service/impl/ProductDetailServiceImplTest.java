package com.josemaba.marquesitasapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceImplTest {

    @Mock
    private ProductDetailRepository productDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductAddonRepository productAddonRepository;

    @Mock
    private ProductDetailMapper productDetailMapper;

    @InjectMocks
    private ProductDetailServiceImpl productDetailService;

    @Test
    void create_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        ProductDetailRequest request = new ProductDetailRequest(UUID.randomUUID(), null, true);
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productDetailService.create(productId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowBusinessRuleViolationException_whenAddonNotAvailable() {
        UUID productId = UUID.randomUUID();
        UUID addonId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").build();
        ProductAddon addon = ProductAddon.builder().id(addonId).name("Jalapenos").price(BigDecimal.ONE).available(false).build();
        ProductDetailRequest request = new ProductDetailRequest(addonId, null, true);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productAddonRepository.findById(addonId)).willReturn(Optional.of(addon));

        assertThatThrownBy(() -> productDetailService.create(productId, request))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(productDetailRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenAddonAlreadyAssignedToProduct() {
        UUID productId = UUID.randomUUID();
        UUID addonId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").build();
        ProductAddon addon = ProductAddon.builder().id(addonId).name("Queso").price(BigDecimal.ONE).available(true).build();
        ProductDetailRequest request = new ProductDetailRequest(addonId, null, true);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productAddonRepository.findById(addonId)).willReturn(Optional.of(addon));
        given(productDetailRepository.existsByProductIdAndAddonId(productId, addonId)).willReturn(true);

        assertThatThrownBy(() -> productDetailService.create(productId, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_shouldSaveDetail_whenValid() {
        UUID productId = UUID.randomUUID();
        UUID addonId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Marquesita").build();
        ProductAddon addon = ProductAddon.builder().id(addonId).name("Queso").price(BigDecimal.ONE).available(true).build();
        ProductDetailRequest request = new ProductDetailRequest(addonId, null, true);
        ProductDetail entity = ProductDetail.builder().available(true).build();
        ProductDetail saved = ProductDetail.builder().id(UUID.randomUUID()).product(product).addon(addon).available(true).build();
        ProductDetailResponse response = new ProductDetailResponse(
                saved.getId(), productId, addonId, "Queso", BigDecimal.ONE, null, BigDecimal.ONE, true);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productAddonRepository.findById(addonId)).willReturn(Optional.of(addon));
        given(productDetailRepository.existsByProductIdAndAddonId(productId, addonId)).willReturn(false);
        given(productDetailMapper.toEntity(request)).willReturn(entity);
        given(productDetailRepository.save(entity)).willReturn(saved);
        given(productDetailMapper.toResponse(saved)).willReturn(response);

        ProductDetailResponse result = productDetailService.create(productId, request);

        assertThat(result).isEqualTo(response);
        assertThat(entity.getProduct()).isEqualTo(product);
        assertThat(entity.getAddon()).isEqualTo(addon);
    }
}
