package com.chandan.freightfox.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Details of an order that could not be assigned to any vehicle.
 */
@Schema(description = "Details of an unassigned delivery order")
public class UnassignedOrderDto {

    @Schema(description = "Identifier of the unassigned order", example = "ORD006")
    private String orderId;

    @Schema(description = "Explanation why the order could not be assigned", example = "Insufficient vehicle capacity available")
    private String reason;

    public UnassignedOrderDto() {
    }

    public UnassignedOrderDto(String orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
