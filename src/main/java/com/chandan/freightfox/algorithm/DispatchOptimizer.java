package com.chandan.freightfox.algorithm;

import com.chandan.freightfox.entity.DeliveryOrder;
import com.chandan.freightfox.entity.OrderPriority;
import com.chandan.freightfox.entity.Vehicle;
import com.chandan.freightfox.util.HaversineCalculator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Pure Java algorithm component that allocates delivery orders to fleet vehicles.
 *
 * Algorithm Strategy: Priority-aware Greedy Nearest Neighbor (O(O * V))
 * 1. Orders are sorted by priority: HIGH -> MEDIUM -> LOW.
 * 2. Orders with equal priority are sorted by orderId ascending for determinism.
 * 3. Each order evaluates all feasible vehicles (remaining capacity >= order package weight).
 * 4. Selects the feasible vehicle with the minimum Haversine distance from its current
 *    position to the order location.
 * 5. Deterministic tie-breaking: selects the vehicle with lexicographically smaller vehicleId.
 * 6. Updates the vehicle's current position, remaining capacity, total load, and route distance.
 * 7. Unassignable orders are recorded in unassignedOrders with reason; processing continues.
 * 8. Every vehicle is included in the output, including vehicles with zero assigned orders.
 * 9. Route distance is strictly: start -> stop 1 -> stop 2 -> ... -> stop N (no return leg).
 *
 * Does not depend on repositories or database access.
 */
@Component
public class DispatchOptimizer {

    private static final double DISTANCE_EPSILON = 1e-7;

    /**
     * Represents the dispatch route and load assigned to a specific vehicle.
     */
    public static class VehicleAssignment {
        private final String vehicleId;
        private final double totalLoad;
        private final double totalDistance;
        private final List<DeliveryOrder> assignedOrders;

        public VehicleAssignment(String vehicleId, double totalLoad, double totalDistance, List<DeliveryOrder> assignedOrders) {
            this.vehicleId = vehicleId;
            this.totalLoad = totalLoad;
            this.totalDistance = totalDistance;
            this.assignedOrders = Collections.unmodifiableList(new ArrayList<>(assignedOrders));
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public double getTotalLoad() {
            return totalLoad;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public List<DeliveryOrder> getAssignedOrders() {
            return assignedOrders;
        }
    }

    /**
     * Represents an order that could not be assigned to any vehicle.
     */
    public static class UnassignedOrder {
        private final DeliveryOrder order;
        private final String reason;

        public UnassignedOrder(DeliveryOrder order, String reason) {
            this.order = order;
            this.reason = reason;
        }

        public DeliveryOrder getOrder() {
            return order;
        }

        public String getOrderId() {
            return order != null ? order.getOrderId() : null;
        }

        public String getReason() {
            return reason;
        }
    }

    /**
     * Overall dispatch result containing vehicle assignments and unassigned orders.
     */
    public static class OptimizationResult {
        private final List<VehicleAssignment> vehicleAssignments;
        private final List<UnassignedOrder> unassignedOrders;

        public OptimizationResult(List<VehicleAssignment> vehicleAssignments, List<UnassignedOrder> unassignedOrders) {
            this.vehicleAssignments = Collections.unmodifiableList(new ArrayList<>(vehicleAssignments));
            this.unassignedOrders = Collections.unmodifiableList(new ArrayList<>(unassignedOrders));
        }

        public List<VehicleAssignment> getVehicleAssignments() {
            return vehicleAssignments;
        }

        public List<UnassignedOrder> getUnassignedOrders() {
            return unassignedOrders;
        }
    }

    /**
     * Internal mutable vehicle state tracking during allocation.
     */
    private static class VehicleState {
        final Vehicle vehicle;
        double currentLat;
        double currentLon;
        double remainingCapacity;
        double totalLoad;
        double totalDistance;
        final List<DeliveryOrder> assignedOrders;

        VehicleState(Vehicle vehicle) {
            this.vehicle = vehicle;
            this.currentLat = vehicle.getCurrentLatitude();
            this.currentLon = vehicle.getCurrentLongitude();
            this.remainingCapacity = vehicle.getCapacity();
            this.totalLoad = 0.0;
            this.totalDistance = 0.0;
            this.assignedOrders = new ArrayList<>();
        }

        boolean canFit(double weight) {
            return this.remainingCapacity >= weight;
        }

        void assign(DeliveryOrder order, double incrementalDistance) {
            this.assignedOrders.add(order);
            this.totalLoad += order.getPackageWeight();
            this.remainingCapacity -= order.getPackageWeight();
            this.totalDistance += incrementalDistance;
            this.currentLat = order.getLatitude();
            this.currentLon = order.getLongitude();
        }

        VehicleAssignment toAssignment() {
            return new VehicleAssignment(
                    vehicle.getVehicleId(),
                    totalLoad,
                    totalDistance,
                    assignedOrders
            );
        }
    }

    /**
     * Executes the priority-aware greedy nearest-neighbor dispatch optimization.
     *
     * @param orders input delivery orders
     * @param vehicles input fleet vehicles
     * @return OptimizationResult with assignments and unassigned orders
     */
    public OptimizationResult optimize(List<DeliveryOrder> orders, List<Vehicle> vehicles) {
        if (vehicles == null || vehicles.isEmpty()) {
            List<UnassignedOrder> unassigned = new ArrayList<>();
            if (orders != null) {
                for (DeliveryOrder order : orders) {
                    unassigned.add(new UnassignedOrder(order, "No fleet vehicles available"));
                }
            }
            return new OptimizationResult(Collections.emptyList(), unassigned);
        }

        // Initialize vehicle states, preserving fleet list order
        List<VehicleState> vehicleStates = new ArrayList<>(vehicles.size());
        for (Vehicle v : vehicles) {
            vehicleStates.add(new VehicleState(v));
        }

        if (orders == null || orders.isEmpty()) {
            List<VehicleAssignment> assignments = new ArrayList<>(vehicleStates.size());
            for (VehicleState state : vehicleStates) {
                assignments.add(state.toAssignment());
            }
            return new OptimizationResult(assignments, Collections.emptyList());
        }

        // Sort orders: HIGH -> MEDIUM -> LOW, tie-broken by orderId ascending
        List<DeliveryOrder> sortedOrders = new ArrayList<>(orders);
        sortedOrders.sort(Comparator
                .comparingInt((DeliveryOrder o) -> getPriorityRank(o.getPriority()))
                .thenComparing(DeliveryOrder::getOrderId, Comparator.nullsLast(String::compareTo))
        );

        List<UnassignedOrder> unassignedOrders = new ArrayList<>();

        for (DeliveryOrder order : sortedOrders) {
            double weight = order.getPackageWeight() != null ? order.getPackageWeight() : 0.0;

            VehicleState bestVehicle = null;
            double bestDistance = Double.MAX_VALUE;

            for (VehicleState state : vehicleStates) {
                if (state.canFit(weight)) {
                    double dist = HaversineCalculator.calculateDistance(
                            state.currentLat, state.currentLon,
                            order.getLatitude(), order.getLongitude()
                    );

                    if (bestVehicle == null) {
                        bestVehicle = state;
                        bestDistance = dist;
                    } else if (dist < bestDistance - DISTANCE_EPSILON) {
                        bestVehicle = state;
                        bestDistance = dist;
                    } else if (Math.abs(dist - bestDistance) <= DISTANCE_EPSILON) {
                        // Deterministic tie-breaking: vehicleId lexicographically smaller
                        if (state.vehicle.getVehicleId().compareTo(bestVehicle.vehicle.getVehicleId()) < 0) {
                            bestVehicle = state;
                            bestDistance = dist;
                        }
                    }
                }
            }

            if (bestVehicle != null) {
                bestVehicle.assign(order, bestDistance);
            } else {
                unassignedOrders.add(new UnassignedOrder(order, "Insufficient vehicle capacity available"));
            }
        }

        List<VehicleAssignment> assignments = new ArrayList<>(vehicleStates.size());
        for (VehicleState state : vehicleStates) {
            assignments.add(state.toAssignment());
        }

        return new OptimizationResult(assignments, unassignedOrders);
    }

    private int getPriorityRank(OrderPriority priority) {
        if (priority == null) {
            return 4;
        }
        return switch (priority) {
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        };
    }
}
