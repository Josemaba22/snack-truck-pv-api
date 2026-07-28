package com.josemaba.marquesitasapi.controller;

import com.josemaba.marquesitasapi.dto.request.ProductDetailRequest;
import com.josemaba.marquesitasapi.dto.response.ProductDetailResponse;
import com.josemaba.marquesitasapi.service.ProductDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/products/{productId}/details")
@RequiredArgsConstructor
@Tag(name = "Product Details", description = "Product-addon association management")
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    @PostMapping
    @Operation(summary = "Attach an addon to a product")
    public ResponseEntity<ProductDetailResponse> create(@PathVariable UUID productId,
            @Valid @RequestBody ProductDetailRequest request) {
        ProductDetailResponse response = productDetailService.create(productId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{detailId}")
    @Operation(summary = "Update a product-addon association")
    public ResponseEntity<ProductDetailResponse> update(@PathVariable UUID productId, @PathVariable UUID detailId,
            @Valid @RequestBody ProductDetailRequest request) {
        return ResponseEntity.ok(productDetailService.update(productId, detailId, request));
    }

    @DeleteMapping("/{detailId}")
    @Operation(summary = "Remove an addon from a product")
    public ResponseEntity<Void> delete(@PathVariable UUID productId, @PathVariable UUID detailId) {
        productDetailService.delete(productId, detailId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{detailId}")
    @Operation(summary = "Get a product-addon association by id")
    public ResponseEntity<ProductDetailResponse> getById(@PathVariable UUID productId, @PathVariable UUID detailId) {
        return ResponseEntity.ok(productDetailService.getById(productId, detailId));
    }

    @GetMapping
    @Operation(summary = "Get all addons available for a product")
    public ResponseEntity<List<ProductDetailResponse>> getAll(@PathVariable UUID productId) {
        return ResponseEntity.ok(productDetailService.getAllByProduct(productId));
    }
}
