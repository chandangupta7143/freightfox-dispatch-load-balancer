package com.chandan.freightfox.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO for batch vehicle input: { "vehicles": [...] }
 * Enforces non-empty batch and uniqueness of vehicleId within the batch.
 */
public class VehicleBatchRequest {

    @NotEmpty(message = "vehicles list must not be empty")
    @Valid
    private List<VehicleRequest> vehicles;

    public VehicleBatchRequest() {
    }

    public VehicleBatchRequest(List<VehicleRequest> vehicles) {
        this.vehicles = vehicles;
    }

    @AssertTrue(message = "Duplicate vehicleId found in incoming batch")
    public boolean isUniqueVehicleIds() {
        if (vehicles == null) {
            return true;
        }
        Set<String> seen = new HashSet<>();
        for (VehicleRequest vehicle : vehicles) {
            if (vehicle != null && vehicle.getVehicleId() != null) {
                String id = vehicle.getVehicleId().trim();
                if (!id.isEmpty() && !seen.add(id)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<VehicleRequest> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<VehicleRequest> vehicles) {
        this.vehicles = vehicles;
    }

    @Override
    public String toString() {
        return "VehicleBatchRequest{" +
                "vehicles=" + (vehicles != null ? vehicles.size() : 0) + " items" +
                '}';
    }
}
