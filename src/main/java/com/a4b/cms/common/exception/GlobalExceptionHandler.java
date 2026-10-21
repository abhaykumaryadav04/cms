package com.a4b.cms.common.exception;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.a4b.cms.common.config.RequestIdFilter;
import com.a4b.cms.common.response.ApiErrorResponse;
import com.a4b.cms.common.response.FieldErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@Component 
public class GlobalExceptionHandler {
   private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle custom application exceptions
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                ex.getStatus(),
                ex.getCode(),
                ex.getMessage(),
                request,
                List.of()
        );
    }

    // Handle DTO validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        List<FieldErrorResponse> fieldErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(this::toFieldErrorResponse)
                        .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "One or more fields are invalid.",
                request,
                fieldErrors
        );
    }

    // Handle malformed JSON request bodies
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_BODY",
                "The request body is missing or malformed.",
                request,
                List.of()
        );
    }

    // Handle database integrity conflicts
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "Database integrity violation. requestId={}",
                getRequestId(request)
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "The request conflicts with existing data.",
                request,
                List.of()
        );
    }

    // Handle unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error(
                "Unexpected error. requestId={}",
                getRequestId(request),
                ex
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred.",
                request,
                List.of()
        );
    }

    private FieldErrorResponse toFieldErrorResponse(
            FieldError error
    ) {
        return new FieldErrorResponse(
                error.getField(),
                error.getDefaultMessage() == null
                        ? "Invalid value"
                        : error.getDefaultMessage()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<FieldErrorResponse> fieldErrors
    ) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                getRequestId(request),
                fieldErrors
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private String getRequestId(
            HttpServletRequest request
    ) {

        Object value = request.getAttribute(
                RequestIdFilter.REQUEST_ID_ATTRIBUTE
        );

        return value == null
                ? UUID.randomUUID().toString()
                : value.toString();
    }
}
