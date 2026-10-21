package com.a4b.cms.common.response;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ApiErrorResponse(
     Instant timestamp,

        int status,

        String code,

        String message,

        String path,

        String requestId,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<FieldErrorResponse> fieldErrors
) {
    public ApiErrorResponse {
        fieldErrors = fieldErrors == null
                ? List.of()
                : List.copyOf(fieldErrors);
    }
}