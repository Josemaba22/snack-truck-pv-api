package com.josemaba.marquesitasapi.controller;

import com.josemaba.marquesitasapi.dto.request.IngredientRequest;
import com.josemaba.marquesitasapi.dto.response.IngredientResponse;
import com.josemaba.marquesitasapi.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Tag(name = "Ingredients", description = "Ingredient catalog management")
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create an ingredient")
    public ResponseEntity<IngredientResponse> create(@Valid @RequestBody IngredientRequest request) {
        IngredientResponse response = ingredientService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update an ingredient")
    public ResponseEntity<IngredientResponse> update(@PathVariable UUID id, @Valid @RequestBody IngredientRequest request) {
        return ResponseEntity.ok(ingredientService.update(id, request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create an ingredient with an optional image (parts: ingredient as JSON, image as JPEG/PNG/WebP)")
    public ResponseEntity<IngredientResponse> createWithImage(
            @Valid @RequestPart("ingredient") IngredientRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        IngredientResponse response = ingredientService.create(request, image);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update an ingredient, optionally replacing its image (parts: ingredient as JSON, image)")
    public ResponseEntity<IngredientResponse> updateWithImage(
            @PathVariable UUID id,
            @Valid @RequestPart("ingredient") IngredientRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(ingredientService.update(id, request, image));
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or replace an ingredient image (JPEG, PNG or WebP)")
    public ResponseEntity<IngredientResponse> uploadImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ingredientService.uploadImage(id, file));
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "Remove an ingredient image")
    public ResponseEntity<IngredientResponse> deleteImage(@PathVariable UUID id) {
        return ResponseEntity.ok(ingredientService.deleteImage(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an ingredient")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an ingredient by id")
    public ResponseEntity<IngredientResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ingredientService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all ingredients")
    public ResponseEntity<List<IngredientResponse>> getAll() {
        return ResponseEntity.ok(ingredientService.getAll());
    }
}
