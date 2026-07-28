package com.josemaba.marquesitasapi.controller;

import com.josemaba.marquesitasapi.dto.request.ProductAddonRequest;
import com.josemaba.marquesitasapi.dto.response.ProductAddonResponse;
import com.josemaba.marquesitasapi.service.ProductAddonService;
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
@RequestMapping("/api/addons")
@RequiredArgsConstructor
@Tag(name = "Addons", description = "Product addon management")
public class ProductAddonController {

    private final ProductAddonService productAddonService;

    @PostMapping
    @Operation(summary = "Create an addon")
    public ResponseEntity<ProductAddonResponse> create(@Valid @RequestBody ProductAddonRequest request) {
        ProductAddonResponse response = productAddonService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an addon")
    public ResponseEntity<ProductAddonResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductAddonRequest request) {
        return ResponseEntity.ok(productAddonService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an addon")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productAddonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an addon by id")
    public ResponseEntity<ProductAddonResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productAddonService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all addons")
    public ResponseEntity<List<ProductAddonResponse>> getAll() {
        return ResponseEntity.ok(productAddonService.getAll());
    }
}
