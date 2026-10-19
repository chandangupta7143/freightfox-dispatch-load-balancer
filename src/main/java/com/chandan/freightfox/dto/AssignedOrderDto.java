package com.chandan.freightfox.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representation of an assigned delivery order in the dispatch plan.
 * Preserves all six order fields from the specification.
 */
@Schema(description = "Assigned order details in vehicle route")
public class AssignedOrderDto {

    @Schema(description = "Unique order identifier", example = "ORD001")
    private String orderId;

    @Schema(description = "Latitude of delivery stop", example = "12.9716")
    private Double latitude;

    @Schema(description = "Longitude of delivery stop", example = "77.5946")
    private Double longitude;

    @Schema(description = "Delivery address", example = "MG Road, Bangalore, Karnataka, India")
    private String address;

    @Schema(description = "Package weight in kg", example = "10.0")
    private Double packageWeight;

    @Schema(description = "Order priority level", example = "HIGH")
    private String priority;

    public AssignedOrderDto() {
    }

    public AssignedOrderDto(String orderId, Double latitude, Double longitude, String address, Double packageWeight, String priority) {
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
}
