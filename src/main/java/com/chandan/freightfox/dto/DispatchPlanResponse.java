package com.chandan.freightfox.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Top-level response structure for GET /api/dispatch/plan.
 */
@Schema(description = "Overall dispatch plan response")
public class DispatchPlanResponse {

    @Schema(description = "List of vehicles and their assigned order routes")
    private List<VehicleDispatchEntry> dispatchPlan = new ArrayList<>();

    @Schema(description = "List of orders that could not be assigned to any vehicle")
    private List<UnassignedOrderDto> unassignedOrders = new ArrayList<>();

    public DispatchPlanResponse() {
    }

    public DispatchPlanResponse(List<VehicleDispatchEntry> dispatchPlan, List<UnassignedOrderDto> unassignedOrders) {
        this.dispatchPlan = dispatchPlan != null ? dispatchPlan : new ArrayList<>();
        this.unassignedOrders = unassignedOrders != null ? unassignedOrders : new ArrayList<>();
    }

    public List<VehicleDispatchEntry> getDispatchPlan() {
        return dispatchPlan;
    }

    public void setDispatchPlan(List<VehicleDispatchEntry> dispatchPlan) {
        this.dispatchPlan = dispatchPlan;
    }

    public List<UnassignedOrderDto> getUnassignedOrders() {
        return unassignedOrders;
    }

    public void setUnassignedOrders(List<UnassignedOrderDto> unassignedOrders) {
        this.unassignedOrders = unassignedOrders;
    }
}
