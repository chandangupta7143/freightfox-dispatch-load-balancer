package com.chandan.freightfox.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * DTO for incoming delivery order input.
 */
public class OrderRequest {

    @NotBlank(message = "orderId is required and must not be blank")
    private String orderId;

    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "latitude must be greater than or equal to -90.0")
    @DecimalMax(value = "90.0", message = "latitude must be less than or equal to 90.0")
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be greater than or equal to -180.0")
    @DecimalMax(value = "180.0", message = "longitude must be less than or equal to 180.0")
    private Double longitude;

    @NotBlank(message = "address is required and must not be blank")
    private String address;

    @NotNull(message = "packageWeight is required")
    @Positive(message = "packageWeight must be greater than 0")
    private Double packageWeight;

    @NotBlank(message = "priority is required and must not be blank")
    @Pattern(regexp = "^(?i)(HIGH|MEDIUM|LOW)$", message = "priority must be HIGH, MEDIUM, or LOW")
    private String priority;

    public OrderRequest() {
    }

    public OrderRequest(String orderId, Double latitude, Double longitude, String address, Double packageWeight, String priority) {
        this.orderId = orderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.packageWeight = packageWeight;
        this.priority = priority;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(Double packageWeight) {
        this.packageWeight = packageWeight;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "orderId='" + orderId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", packageWeight=" + packageWeight +
                ", priority='" + priority + '\'' +
                '}';
    }
}
