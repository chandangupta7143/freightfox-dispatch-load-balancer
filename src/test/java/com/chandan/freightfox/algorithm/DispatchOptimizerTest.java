package com.chandan.freightfox.algorithm;

import com.chandan.freightfox.entity.DeliveryOrder;
import com.chandan.freightfox.entity.OrderPriority;
import com.chandan.freightfox.entity.Vehicle;
import com.chandan.freightfox.util.HaversineCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatchOptimizerTest {

    private DispatchOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new DispatchOptimizer();
    }

    @Test
    @DisplayName("HIGH orders are processed before MEDIUM and LOW orders")
    void highPriorityProcessedFirst() {
        // Vehicle has capacity 30.
        // Orders: 1 LOW (20kg), 1 MEDIUM (20kg), 1 HIGH (20kg).
        // Only one order can fit. The HIGH order must be chosen!
        Vehicle vehicle = new Vehicle("VEH_1", 30.0, 10.0, 10.0, "Depot");

        List<DeliveryOrder> orders = List.of(
                new DeliveryOrder("ORD_LOW", 10.1, 10.1, "Addr 1", 20.0, OrderPriority.LOW),
                new DeliveryOrder("ORD_MED", 10.1, 10.1, "Addr 2", 20.0, OrderPriority.MEDIUM),
                new DeliveryOrder("ORD_HIGH", 10.1, 10.1, "Addr 3", 20.0, OrderPriority.HIGH)
        );

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(orders, List.of(vehicle));

        DispatchOptimizer.VehicleAssignment assignment = result.getVehicleAssignments().get(0);
        assertEquals(1, assignment.getAssignedOrders().size());
        assertEquals("ORD_HIGH", assignment.getAssignedOrders().get(0).getOrderId());
        assertEquals(20.0, assignment.getTotalLoad());

        // The remaining 2 orders must be in unassigned
        assertEquals(2, result.getUnassignedOrders().size());
        Set<String> unassignedIds = new HashSet<>();
        for (DispatchOptimizer.UnassignedOrder u : result.getUnassignedOrders()) {
            unassignedIds.add(u.getOrderId());
        }
        assertTrue(unassignedIds.contains("ORD_MED"));
        assertTrue(unassignedIds.contains("ORD_LOW"));
    }

    @Test
    @DisplayName("Same-priority orders follow deterministic orderId ascending ordering")
    void samePriorityDeterministicOrderId() {
        // Vehicle can take only 1 order of 15kg. Two HIGH orders: ORD_B (15kg) and ORD_A (15kg).
        // ORD_A must be processed before ORD_B and get assigned.
        Vehicle vehicle = new Vehicle("VEH_1", 20.0, 10.0, 10.0, "Depot");

        List<DeliveryOrder> orders = List.of(
                new DeliveryOrder("ORD_B", 10.1, 10.1, "Addr B", 15.0, OrderPriority.HIGH),
                new DeliveryOrder("ORD_A", 10.1, 10.1, "Addr A", 15.0, OrderPriority.HIGH)
        );

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(orders, List.of(vehicle));

        assertEquals(1, result.getVehicleAssignments().get(0).getAssignedOrders().size());
        assertEquals("ORD_A", result.getVehicleAssignments().get(0).getAssignedOrders().get(0).getOrderId());

        assertEquals(1, result.getUnassignedOrders().size());
        assertEquals("ORD_B", result.getUnassignedOrders().get(0).getOrderId());
    }

    @Test
    @DisplayName("Exact-capacity assignment succeeds and fills vehicle completely")
    void exactCapacityAssignmentSucceeds() {
        Vehicle vehicle = new Vehicle("VEH_1", 50.0, 10.0, 10.0, "Depot");
        DeliveryOrder order = new DeliveryOrder("ORD_EXACT", 10.1, 10.1, "Addr", 50.0, OrderPriority.HIGH);

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(List.of(order), List.of(vehicle));

        DispatchOptimizer.VehicleAssignment assignment = result.getVehicleAssignments().get(0);
        assertEquals(50.0, assignment.getTotalLoad());
        assertEquals(1, assignment.getAssignedOrders().size());
        assertTrue(result.getUnassignedOrders().isEmpty());
    }

    @Test
    @DisplayName("Oversized order exceeding all vehicles is unassigned")
    void oversizedOrderUnassigned() {
        Vehicle v1 = new Vehicle("VEH_1", 40.0, 10.0, 10.0, "Depot 1");
        Vehicle v2 = new Vehicle("VEH_2", 60.0, 10.0, 10.0, "Depot 2");
        DeliveryOrder heavyOrder = new DeliveryOrder("ORD_HEAVY", 10.1, 10.1, "Addr", 70.0, OrderPriority.HIGH);

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(List.of(heavyOrder), List.of(v1, v2));

        assertEquals(1, result.getUnassignedOrders().size());
        assertEquals("ORD_HEAVY", result.getUnassignedOrders().get(0).getOrderId());
        assertEquals(0.0, result.getVehicleAssignments().get(0).getTotalLoad());
        assertEquals(0.0, result.getVehicleAssignments().get(1).getTotalLoad());
    }

    @Test
    @DisplayName("Processing continues after an unassignable order")
    void processingContinuesAfterUnassignedOrder() {
        // Vehicle has capacity 30.
        // Order 1: HIGH 50kg (cannot fit -> unassigned)
        // Order 2: MEDIUM 20kg (can fit -> assigned!)
        Vehicle vehicle = new Vehicle("VEH_1", 30.0, 10.0, 10.0, "Depot");
        List<DeliveryOrder> orders = List.of(
                new DeliveryOrder("ORD_1_HIGH", 10.1, 10.1, "Addr 1", 50.0, OrderPriority.HIGH),
                new DeliveryOrder("ORD_2_MED", 10.2, 10.2, "Addr 2", 20.0, OrderPriority.MEDIUM)
        );

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(orders, List.of(vehicle));

        assertEquals(1, result.getUnassignedOrders().size());
        assertEquals("ORD_1_HIGH", result.getUnassignedOrders().get(0).getOrderId());

        DispatchOptimizer.VehicleAssignment assignment = result.getVehicleAssignments().get(0);
        assertEquals(1, assignment.getAssignedOrders().size());
        assertEquals("ORD_2_MED", assignment.getAssignedOrders().get(0).getOrderId());
        assertEquals(20.0, assignment.getTotalLoad());
    }

    @Test
    @DisplayName("Nearest feasible vehicle is selected among candidates")
    void nearestFeasibleVehicleSelected() {
        // Vehicle A is at (0.0, 0.0), capacity 100
        // Vehicle B is at (10.0, 10.0), capacity 100
        // Order is at (0.01, 0.01) -> much closer to Vehicle A!
        Vehicle vehA = new Vehicle("VEH_A", 100.0, 0.0, 0.0, "Loc A");
        Vehicle vehB = new Vehicle("VEH_B", 100.0, 10.0, 10.0, "Loc B");
        DeliveryOrder order = new DeliveryOrder("ORD_1", 0.01, 0.01, "Near A", 10.0, OrderPriority.HIGH);

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(List.of(order), List.of(vehA, vehB));

        DispatchOptimizer.VehicleAssignment assignA = result.getVehicleAssignments().stream()
                .filter(a -> a.getVehicleId().equals("VEH_A")).findFirst().orElseThrow();
        DispatchOptimizer.VehicleAssignment assignB = result.getVehicleAssignments().stream()
                .filter(a -> a.getVehicleId().equals("VEH_B")).findFirst().orElseThrow();

        assertEquals(1, assignA.getAssignedOrders().size());
        assertEquals("ORD_1", assignA.getAssignedOrders().get(0).getOrderId());
        assertEquals(0, assignB.getAssignedOrders().size());
    }

    @Test
    @DisplayName("Equal distance tie-breaking selects vehicle with lexicographically smaller vehicleId")
    void equalDistanceTieBreakerSelectsSmallerId() {
        // Both vehicles at exact same location (0.0, 0.0) with same capacity
        // Order is at (1.0, 1.0)
        // Vehicle "VEH_2" vs "VEH_1" -> VEH_1 should be selected!
        Vehicle veh2 = new Vehicle("VEH_2", 100.0, 0.0, 0.0, "Depot");
        Vehicle veh1 = new Vehicle("VEH_1", 100.0, 0.0, 0.0, "Depot");
        DeliveryOrder order = new DeliveryOrder("ORD_1", 1.0, 1.0, "Drop", 10.0, OrderPriority.HIGH);

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(List.of(order), List.of(veh2, veh1));

        DispatchOptimizer.VehicleAssignment assign1 = result.getVehicleAssignments().stream()
                .filter(a -> a.getVehicleId().equals("VEH_1")).findFirst().orElseThrow();
        DispatchOptimizer.VehicleAssignment assign2 = result.getVehicleAssignments().stream()
                .filter(a -> a.getVehicleId().equals("VEH_2")).findFirst().orElseThrow();

        assertEquals(1, assign1.getAssignedOrders().size());
        assertEquals(0, assign2.getAssignedOrders().size());
    }

    @Test
    @DisplayName("Route distance matches consecutive stops without return leg")
    void routeDistanceMatchesStopsWithoutReturn() {
        // Vehicle start at (0.0, 0.0)
        // Stop 1 at (1.0, 0.0)
        // Stop 2 at (2.0, 0.0)
        Vehicle vehicle = new Vehicle("VEH_1", 100.0, 0.0, 0.0, "Start");
        DeliveryOrder o1 = new DeliveryOrder("ORD_1", 1.0, 0.0, "Stop 1", 10.0, OrderPriority.HIGH);
        DeliveryOrder o2 = new DeliveryOrder("ORD_2", 2.0, 0.0, "Stop 2", 10.0, OrderPriority.HIGH);

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(List.of(o1, o2), List.of(vehicle));

        DispatchOptimizer.VehicleAssignment assignment = result.getVehicleAssignments().get(0);
        assertEquals(2, assignment.getAssignedOrders().size());

        double leg1 = HaversineCalculator.calculateDistance(0.0, 0.0, 1.0, 0.0);
        double leg2 = HaversineCalculator.calculateDistance(1.0, 0.0, 2.0, 0.0);
        double expectedDistance = leg1 + leg2;

        assertEquals(expectedDistance, assignment.getTotalDistance(), 1e-6);
    }

    @Test
    @DisplayName("Vehicles with zero assigned orders remain in the result with 0 distance and 0 load")
    void unassignedVehiclesRemainInResult() {
        Vehicle v1 = new Vehicle("VEH_1", 100.0, 0.0, 0.0, "Depot 1");
        Vehicle v2 = new Vehicle("VEH_2", 100.0, 50.0, 50.0, "Depot 2");
        DeliveryOrder order = new DeliveryOrder("ORD_1", 0.01, 0.01, "Near 1", 10.0, OrderPriority.HIGH);

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(List.of(order), List.of(v1, v2));

        assertEquals(2, result.getVehicleAssignments().size());

        DispatchOptimizer.VehicleAssignment assign2 = result.getVehicleAssignments().stream()
                .filter(a -> a.getVehicleId().equals("VEH_2")).findFirst().orElseThrow();
        assertEquals(0.0, assign2.getTotalLoad());
        assertEquals(0.0, assign2.getTotalDistance());
        assertTrue(assign2.getAssignedOrders().isEmpty());
    }

    @Test
    @DisplayName("Every order appears exactly once across assigned and unassigned")
    void everyOrderAccountedForExactlyOnce() {
        Vehicle v1 = new Vehicle("VEH_1", 30.0, 0.0, 0.0, "Depot");
        List<DeliveryOrder> orders = List.of(
                new DeliveryOrder("ORD_1", 0.1, 0.1, "A1", 15.0, OrderPriority.HIGH),
                new DeliveryOrder("ORD_2", 0.2, 0.2, "A2", 15.0, OrderPriority.HIGH),
                new DeliveryOrder("ORD_3", 0.3, 0.3, "A3", 15.0, OrderPriority.MEDIUM),
                new DeliveryOrder("ORD_4", 0.4, 0.4, "A4", 15.0, OrderPriority.LOW)
        );

        DispatchOptimizer.OptimizationResult result = optimizer.optimize(orders, List.of(v1));

        Set<String> allEncountered = new HashSet<>();

        for (DispatchOptimizer.VehicleAssignment va : result.getVehicleAssignments()) {
            for (DeliveryOrder o : va.getAssignedOrders()) {
                assertTrue(allEncountered.add(o.getOrderId()), "Duplicate assigned order: " + o.getOrderId());
            }
        }

        for (DispatchOptimizer.UnassignedOrder uo : result.getUnassignedOrders()) {
            assertTrue(allEncountered.add(uo.getOrderId()), "Duplicate order across assigned and unassigned: " + uo.getOrderId());
        }

        assertEquals(4, allEncountered.size());
    }

    @Test
    @DisplayName("Empty order list returns all vehicles with zero load and zero distance")
    void emptyOrdersHandledSafely() {
        Vehicle v = new Vehicle("VEH_1", 100.0, 10.0, 10.0, "Depot");
        DispatchOptimizer.OptimizationResult result = optimizer.optimize(Collections.emptyList(), List.of(v));

        assertEquals(1, result.getVehicleAssignments().size());
        assertEquals(0.0, result.getVehicleAssignments().get(0).getTotalLoad());
        assertEquals(0.0, result.getVehicleAssignments().get(0).getTotalDistance());
        assertTrue(result.getUnassignedOrders().isEmpty());
    }

    @Test
    @DisplayName("Empty vehicle list puts all orders into unassignedOrders")
    void emptyVehiclesAllOrdersUnassigned() {
        DeliveryOrder o = new DeliveryOrder("ORD_1", 10.0, 10.0, "Addr", 10.0, OrderPriority.HIGH);
        DispatchOptimizer.OptimizationResult result = optimizer.optimize(List.of(o), Collections.emptyList());

        assertTrue(result.getVehicleAssignments().isEmpty());
        assertEquals(1, result.getUnassignedOrders().size());
        assertEquals("ORD_1", result.getUnassignedOrders().get(0).getOrderId());
    }
}
