package com.chandan.freightfox.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a vehicle's plan entry in the dispatch output.
 */
@Schema(description = "Vehicle route assignment details")
public class VehicleDispatchEntry {

    @Schema(description = "Identifier of the vehicle", example = "VEH001")
    private String vehicleId;

    @Schema(description = "Total cumulative load assigned to the vehicle in kg", example = "10.0")
    private Double totalLoad;

    @Schema(description = "Total route distance formatted with unit", example = "5.23 km")
    private String totalDistance;

    @Schema(description = "Ordered sequence of delivery stops assigned to this vehicle")
    private List<AssignedOrderDto> assignedOrders = new ArrayList<>();

    public VehicleDispatchEntry() {
    }

    public VehicleDispatchEntry(String vehicleId, Double totalLoad, String totalDistance, List<AssignedOrderDto> assignedOrders) {
        this.vehicleId = vehicleId;
        this.totalLoad = totalLoad;
        this.totalDistance = totalDistance;
        this.assignedOrders = assignedOrders != null ? assignedOrders : new ArrayList<>();
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getTotalLoad() {
        return totalLoad;
    }

    public void setTotalLoad(Double totalLoad) {
        this.totalLoad = totalLoad;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public List<AssignedOrderDto> getAssignedOrders() {
        return assignedOrders;
    }

    public void setAssignedOrders(List<AssignedOrderDto> assignedOrders) {
        this.assignedOrders = assignedOrders;
    }
}
