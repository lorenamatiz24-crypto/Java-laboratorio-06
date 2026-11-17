package com.example.petmatch_lab.api.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.petmatch_lab.api.controller.PetRestController;
import com.example.petmatch_lab.api.controller.SupportRequestRestController;
import com.example.petmatch_lab.api.dto.ApiErrorResponse;
import com.example.petmatch_lab.exception.BusinessRuleException;
import com.example.petmatch_lab.exception.ResourceNotFoundException;

@RestControllerAdvice(assignableTypes = {
    PetRestController.class,
    SupportRequestRestController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            String message = error.getDefaultMessage() == null
                ? "Invalid field value"
                : error.getDefaultMessage();
            errors.putIfAbsent(error.getField(), message);
        }

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            "validation_failed",
            "The request contains invalid fields",
            errors
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody() {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            "invalid_json",
            "The request body is not valid JSON for this operation",
            Map.of()
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPathVariable() {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            "invalid_parameter",
            "A path parameter has an invalid value",
            Map.of()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidArgument() {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            "invalid_request",
            "The request contains invalid data",
            Map.of()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound() {
        return ResponseEntity.status(404).body(new ApiErrorResponse(
            "not_found",
            "The requested resource does not exist",
            Map.of()
        ));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule() {
        return ResponseEntity.status(409).body(new ApiErrorResponse(
            "business_conflict",
            "The operation conflicts with the current resource state",
            Map.of()
        ));
    }
}
