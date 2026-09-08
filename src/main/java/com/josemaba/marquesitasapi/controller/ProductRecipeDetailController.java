package com.josemaba.marquesitasapi.controller;

import com.josemaba.marquesitasapi.dto.request.ProductRecipeDetailRequest;
import com.josemaba.marquesitasapi.dto.response.ProductRecipeDetailResponse;
import com.josemaba.marquesitasapi.service.ProductRecipeDetailService;
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
@RequestMapping("/api/products/{productId}/recipe-details")
@RequiredArgsConstructor
@Tag(name = "Product Recipe Details", description = "Product-ingredient recipe management")
public class ProductRecipeDetailController {

    private final ProductRecipeDetailService productRecipeDetailService;

    @PostMapping
    @Operation(summary = "Attach an ingredient to a product's recipe")
    public ResponseEntity<ProductRecipeDetailResponse> create(@PathVariable UUID productId,
            @Valid @RequestBody ProductRecipeDetailRequest request) {
        ProductRecipeDetailResponse response = productRecipeDetailService.create(productId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{detailId}")
    @Operation(summary = "Update a product-ingredient recipe entry")
    public ResponseEntity<ProductRecipeDetailResponse> update(@PathVariable UUID productId, @PathVariable UUID detailId,
            @Valid @RequestBody ProductRecipeDetailRequest request) {
        return ResponseEntity.ok(productRecipeDetailService.update(productId, detailId, request));
    }

    @DeleteMapping("/{detailId}")
    @Operation(summary = "Remove an ingredient from a product's recipe")
    public ResponseEntity<Void> delete(@PathVariable UUID productId, @PathVariable UUID detailId) {
        productRecipeDetailService.delete(productId, detailId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{detailId}")
    @Operation(summary = "Get a product-ingredient recipe entry by id")
    public ResponseEntity<ProductRecipeDetailResponse> getById(@PathVariable UUID productId, @PathVariable UUID detailId) {
        return ResponseEntity.ok(productRecipeDetailService.getById(productId, detailId));
    }

    @GetMapping
    @Operation(summary = "Get the full recipe (base ingredients and extras) for a product")
    public ResponseEntity<List<ProductRecipeDetailResponse>> getAll(@PathVariable UUID productId) {
        return ResponseEntity.ok(productRecipeDetailService.getAllByProduct(productId));
    }
}
