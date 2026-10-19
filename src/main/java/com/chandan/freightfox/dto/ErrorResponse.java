package com.chandan.freightfox.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Consistent error response structure returned for client or server errors.
 */
@Schema(description = "Standard error response payload")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "Error indicator", example = "error")
    private String status;

    @Schema(description = "High-level summary of the error", example = "Validation failed")
    private String message;

    @Schema(description = "Detailed list of field or validation errors", example = "[\"latitude must be between -90.0 and 90.0\"]")
    private List<String> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(String status, String message, List<String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
