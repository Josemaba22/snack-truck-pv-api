package com.josemaba.marquesitasapi.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> errors) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path, null);
    }

    public static ErrorResponse ofValidation(HttpStatus status, String message, String path, List<FieldErrorDetail> errors) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path, errors);
    }

    public record FieldErrorDetail(String field, String message) {
    }
}
