package com.josemaba.marquesitasapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.josemaba.marquesitasapi.dto.request.ProductAddonRequest;
import com.josemaba.marquesitasapi.dto.response.ProductAddonResponse;
import com.josemaba.marquesitasapi.entity.ProductAddon;
import com.josemaba.marquesitasapi.exception.ResourceNotFoundException;
import com.josemaba.marquesitasapi.mapper.ProductAddonMapper;
import com.josemaba.marquesitasapi.repository.ProductAddonRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductAddonServiceImplTest {

    @Mock
    private ProductAddonRepository productAddonRepository;

    @Mock
    private ProductAddonMapper productAddonMapper;

    @InjectMocks
    private ProductAddonServiceImpl productAddonService;

    @Test
    void create_shouldSaveAddon() {
        ProductAddonRequest request = new ProductAddonRequest("Queso extra", new BigDecimal("10.00"), true);
        ProductAddon entity = ProductAddon.builder().name("Queso extra").price(new BigDecimal("10.00")).available(true).build();
        ProductAddon saved = ProductAddon.builder().id(UUID.randomUUID()).name("Queso extra").price(new BigDecimal("10.00")).available(true).build();
        ProductAddonResponse response = new ProductAddonResponse(saved.getId(), "Queso extra", new BigDecimal("10.00"), true);

        given(productAddonMapper.toEntity(request)).willReturn(entity);
        given(productAddonRepository.save(entity)).willReturn(saved);
        given(productAddonMapper.toResponse(saved)).willReturn(response);

        ProductAddonResponse result = productAddonService.create(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenAddonDoesNotExist() {
        UUID id = UUID.randomUUID();
        ProductAddonRequest request = new ProductAddonRequest("Tocino", BigDecimal.ONE, true);
        given(productAddonRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productAddonService.update(id, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldRemoveAddon_whenExists() {
        UUID id = UUID.randomUUID();
        ProductAddon addon = ProductAddon.builder().id(id).name("Tocino").price(BigDecimal.ONE).available(true).build();
        given(productAddonRepository.findById(id)).willReturn(Optional.of(addon));

        productAddonService.delete(id);

        verify(productAddonRepository).delete(addon);
    }
}
