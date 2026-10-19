package com.chandan.freightfox.algorithm;

import com.chandan.freightfox.entity.DeliveryOrder;
import com.chandan.freightfox.entity.OrderPriority;
import com.chandan.freightfox.entity.Vehicle;
import com.chandan.freightfox.util.HaversineCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleDatasetVerificationTest {

    private List<DeliveryOrder> createSampleOrders() {
        List<DeliveryOrder> list = new ArrayList<>();
        list.add(new DeliveryOrder("ORD001", 28.6139, 77.2090, "Connaught Place, Delhi, India", 15.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD002", 28.6139, 77.2090, "Connaught Place, Delhi, India", 10.0, OrderPriority.MEDIUM));
        list.add(new DeliveryOrder("ORD003", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD004", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 25.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD005", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 30.0, OrderPriority.MEDIUM));
        list.add(new DeliveryOrder("ORD006", 28.6139, 77.2090, "Connaught Place, Delhi, India", 40.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD007", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD008", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 25.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD009", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, OrderPriority.MEDIUM));
        list.add(new DeliveryOrder("ORD010", 28.6139, 77.2090, "Connaught Place, Delhi, India", 30.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD011", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD012", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 10.0, OrderPriority.MEDIUM));
        list.add(new DeliveryOrder("ORD013", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 25.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD014", 28.6139, 77.2090, "Connaught Place, Delhi, India", 15.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD015", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD016", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD017", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, OrderPriority.MEDIUM));
        list.add(new DeliveryOrder("ORD018", 28.6139, 77.2090, "Connaught Place, Delhi, India", 10.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD019", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD020", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD021", 28.6139, 77.2090, "Connaught Place, Delhi, India", 25.0, OrderPriority.MEDIUM));
        list.add(new DeliveryOrder("ORD022", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD023", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD024", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD025", 28.6139, 77.2090, "Connaught Place, Delhi, India", 20.0, OrderPriority.MEDIUM));
        list.add(new DeliveryOrder("ORD026", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 25.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD027", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 20.0, OrderPriority.MEDIUM));
        list.add(new DeliveryOrder("ORD028", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, OrderPriority.LOW));
        list.add(new DeliveryOrder("ORD029", 28.6139, 77.2090, "Connaught Place, Delhi, India", 30.0, OrderPriority.HIGH));
        list.add(new DeliveryOrder("ORD030", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, OrderPriority.LOW));
        return list;
    }

    private List<Vehicle> createSampleVehicles() {
        List<Vehicle> list = new ArrayList<>();
        list.add(new Vehicle("VEH001", 100.0, 28.7041, 77.1025, "Karol Bagh, Delhi, India"));
        list.add(new Vehicle("VEH002", 80.0, 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India"));
        list.add(new Vehicle("VEH003", 120.0, 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India"));
        list.add(new Vehicle("VEH004", 90.0, 28.6139, 77.2090, "Connaught Place, Delhi, India"));
        list.add(new Vehicle("VEH005", 110.0, 28.7041, 77.1025, "Karol Bagh, Delhi, India"));
        return list;
    }

    @Test
    @DisplayName("Sample dataset verification: 30 orders, 5 vehicles satisfy all assignment constraints")
    void verifySampleDatasetOptimization() {
        List<DeliveryOrder> sampleOrders = createSampleOrders();
        List<Vehicle> sampleVehicles = createSampleVehicles();

        assertEquals(30, sampleOrders.size());
        assertEquals(5, sampleVehicles.size());

        double totalOrderWeight = sampleOrders.stream().mapToDouble(DeliveryOrder::getPackageWeight).sum();
        double totalFleetCapacity = sampleVehicles.stream().mapToDouble(Vehicle::getCapacity).sum();

        assertEquals(645.0, totalOrderWeight, 1e-9);
        assertEquals(500.0, totalFleetCapacity, 1e-9);

        DispatchOptimizer optimizer = new DispatchOptimizer();
        DispatchOptimizer.OptimizationResult result = optimizer.optimize(sampleOrders, sampleVehicles);

        // Invariant 1: All 5 vehicles are included in the result
        assertEquals(5, result.getVehicleAssignments().size());

        // Invariant 2: Every order appears exactly once across assigned and unassigned
        Set<String> accountedOrderIds = new HashSet<>();
        double assignedLoadSum = 0.0;

        Map<String, Vehicle> vehicleMap = new HashMap<>();
        for (Vehicle v : sampleVehicles) {
            vehicleMap.put(v.getVehicleId(), v);
        }

        for (DispatchOptimizer.VehicleAssignment assignment : result.getVehicleAssignments()) {
            Vehicle originalVehicle = vehicleMap.get(assignment.getVehicleId());
            assertNotNull(originalVehicle);

            // Invariant 3: Capacity constraint is strictly respected
            assertTrue(assignment.getTotalLoad() <= originalVehicle.getCapacity(),
                    "Vehicle " + assignment.getVehicleId() + " overloaded: load "
                            + assignment.getTotalLoad() + " > capacity " + originalVehicle.getCapacity());

            // Invariant 4: totalLoad equals sum of assigned order package weights
            double calculatedLoad = 0.0;
            double currentLat = originalVehicle.getCurrentLatitude();
            double currentLon = originalVehicle.getCurrentLongitude();
            double calculatedDistance = 0.0;

            for (DeliveryOrder order : assignment.getAssignedOrders()) {
                assertTrue(accountedOrderIds.add(order.getOrderId()),
                        "Order assigned more than once: " + order.getOrderId());
                calculatedLoad += order.getPackageWeight();

                double legDist = HaversineCalculator.calculateDistance(
                        currentLat, currentLon, order.getLatitude(), order.getLongitude()
                );
                calculatedDistance += legDist;
                currentLat = order.getLatitude();
                currentLon = order.getLongitude();
            }

            assertEquals(calculatedLoad, assignment.getTotalLoad(), 1e-9);
            // Invariant 5: Route distance accurately matches consecutive legs
            assertEquals(calculatedDistance, assignment.getTotalDistance(), 1e-6);

            assignedLoadSum += assignment.getTotalLoad();
        }

        double unassignedWeightSum = 0.0;
        for (DispatchOptimizer.UnassignedOrder unassigned : result.getUnassignedOrders()) {
            assertTrue(accountedOrderIds.add(unassigned.getOrderId()),
                    "Order appears both as assigned and unassigned: " + unassigned.getOrderId());
            unassignedWeightSum += unassigned.getOrder().getPackageWeight();
            assertFalse(unassigned.getReason().isBlank());
        }

        // Invariant 6: All 30 orders accounted for
        assertEquals(30, accountedOrderIds.size());

        // Invariant 7: Total load conservation (assigned + unassigned = 645 kg)
        assertEquals(totalOrderWeight, assignedLoadSum + unassignedWeightSum, 1e-9);

        // Invariant 8: Because total order weight (645 kg) exceeds total capacity (500 kg),
        // at least 145 kg must remain unassigned
        assertTrue(unassignedWeightSum >= 145.0,
                "Expected at least 145 kg unassigned, but got: " + unassignedWeightSum);

        // Invariant 9: Assigned load cannot exceed total fleet capacity (500 kg)
        assertTrue(assignedLoadSum <= totalFleetCapacity,
                "Assigned load (" + assignedLoadSum + ") exceeded fleet capacity (" + totalFleetCapacity + ")");
    }
}
