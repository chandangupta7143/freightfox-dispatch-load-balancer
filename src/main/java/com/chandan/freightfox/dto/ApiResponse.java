package com.chandan.freightfox.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard API response wrapper for batch acceptance endpoints.
 */
@Schema(description = "Standard API response")
public class ApiResponse {

    @Schema(description = "Response status message", example = "Delivery orders accepted.")
    private String message;

    @Schema(description = "Response status code name", example = "success")
    private String status;

    public ApiResponse() {
    }

    public ApiResponse(String message, String status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static ApiResponse success(String message) {
        return new ApiResponse(message, "success");
    }
}
