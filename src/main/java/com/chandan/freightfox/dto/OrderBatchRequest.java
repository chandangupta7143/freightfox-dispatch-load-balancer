package com.chandan.freightfox.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO for batch order input: { "orders": [...] }
 * Enforces non-empty batch and uniqueness of orderId within the batch.
 */
public class OrderBatchRequest {

    @NotEmpty(message = "orders list must not be empty")
    @Valid
    private List<OrderRequest> orders;

    public OrderBatchRequest() {
    }

    public OrderBatchRequest(List<OrderRequest> orders) {
        this.orders = orders;
    }

    @AssertTrue(message = "Duplicate orderId found in incoming batch")
    public boolean isUniqueOrderIds() {
        if (orders == null) {
            return true;
        }
        Set<String> seen = new HashSet<>();
        for (OrderRequest order : orders) {
            if (order != null && order.getOrderId() != null) {
                String id = order.getOrderId().trim();
                if (!id.isEmpty() && !seen.add(id)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<OrderRequest> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderRequest> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "OrderBatchRequest{" +
                "orders=" + (orders != null ? orders.size() : 0) + " items" +
                '}';
    }
}
