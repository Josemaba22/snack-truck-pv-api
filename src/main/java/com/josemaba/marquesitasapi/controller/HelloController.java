package com.josemaba.marquesitasapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello")
@RequiredArgsConstructor
@Tag(name = "Hello", description = "Order management")
public class HelloController {

    @GetMapping("")
    @Operation(summary = "Get an order by id")
    public ResponseEntity<String> getHello() {
        return ResponseEntity.ok("Hello");
    }

}
