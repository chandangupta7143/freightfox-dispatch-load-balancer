package com.chandan.freightfox.service;

import com.chandan.freightfox.dto.OrderBatchRequest;
import com.chandan.freightfox.dto.OrderRequest;
import com.chandan.freightfox.dto.VehicleBatchRequest;
import com.chandan.freightfox.dto.VehicleRequest;
import com.chandan.freightfox.entity.DeliveryOrder;
import com.chandan.freightfox.entity.OrderPriority;
import com.chandan.freightfox.entity.Vehicle;
import com.chandan.freightfox.repository.OrderRepository;
import com.chandan.freightfox.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live persistence integration tests for DeliveryOrder and Vehicle repositories and services.
 * Uses safe ID tracking to clean up only test-generated rows without affecting unrelated data.
 */
@SpringBootTest
class PersistenceIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VehicleService vehicleService;

    private final Set<String> createdOrderIds = new HashSet<>();
    private final Set<String> createdVehicleIds = new HashSet<>();

    @AfterEach
    void tearDown() {
        if (!createdOrderIds.isEmpty()) {
            orderRepository.deleteAllById(createdOrderIds);
            createdOrderIds.clear();
        }
        if (!createdVehicleIds.isEmpty()) {
            vehicleRepository.deleteAllById(createdVehicleIds);
            createdVehicleIds.clear();
        }
    }

    private DeliveryOrder persistOrder(DeliveryOrder order) {
        createdOrderIds.add(order.getOrderId());
        return orderRepository.save(order);
    }

    private Vehicle persistVehicle(Vehicle vehicle) {
        createdVehicleIds.add(vehicle.getVehicleId());
        return vehicleRepository.save(vehicle);
    }

    @Test
    @DisplayName("Save new DeliveryOrder and verify database retrieval")
    void saveAndRetrieveDeliveryOrder() {
        DeliveryOrder order = new DeliveryOrder(
                "TEST_ORD_01",
                28.6139,
                77.2090,
                "Connaught Place, Delhi",
                15.0,
                OrderPriority.HIGH
        );

        persistOrder(order);

        Optional<DeliveryOrder> found = orderRepository.findById("TEST_ORD_01");
        assertTrue(found.isPresent());
        assertEquals("TEST_ORD_01", found.get().getOrderId());
        assertEquals(15.0, found.get().getPackageWeight());
        assertEquals(OrderPriority.HIGH, found.get().getPriority());
    }

    @Test
    @DisplayName("Upsert DeliveryOrder with existing ID updates the existing record")
    void upsertDeliveryOrderUpdatesExisting() {
        DeliveryOrder initial = new DeliveryOrder(
                "TEST_ORD_UP",
                28.6139,
                77.2090,
                "Old Address, Delhi",
                10.0,
                OrderPriority.LOW
        );
        persistOrder(initial);

        // Same ID, updated details
        DeliveryOrder updated = new DeliveryOrder(
                "TEST_ORD_UP",
                28.7041,
                77.1025,
                "New Address, Delhi",
                25.0,
                OrderPriority.HIGH
        );
        persistOrder(updated);

        // Verify record has updated values without duplicating
        Optional<DeliveryOrder> retrieved = orderRepository.findById("TEST_ORD_UP");
        assertTrue(retrieved.isPresent());
        assertEquals(25.0, retrieved.get().getPackageWeight());
        assertEquals("New Address, Delhi", retrieved.get().getAddress());
        assertEquals(OrderPriority.HIGH, retrieved.get().getPriority());
    }

    @Test
    @DisplayName("Batch save DeliveryOrders through OrderService persists all items")
    void batchSaveOrdersThroughService() {
        List<OrderRequest> list = new ArrayList<>();
        list.add(new OrderRequest("BATCH_ORD_1", 28.6139, 77.2090, "Delhi", 15.0, "HIGH"));
        list.add(new OrderRequest("BATCH_ORD_2", 28.5355, 77.3910, "Noida", 20.0, "MEDIUM"));
        list.add(new OrderRequest("BATCH_ORD_3", 28.4595, 77.0266, "Gurgaon", 30.0, "LOW"));

        for (OrderRequest r : list) {
            createdOrderIds.add(r.getOrderId());
        }

        OrderBatchRequest batch = new OrderBatchRequest(list);
        List<DeliveryOrder> saved = orderService.saveOrderBatch(batch);

        assertEquals(3, saved.size());
        assertTrue(orderRepository.findById("BATCH_ORD_1").isPresent());
        assertTrue(orderRepository.findById("BATCH_ORD_2").isPresent());
        assertTrue(orderRepository.findById("BATCH_ORD_3").isPresent());
    }

    @Test
    @DisplayName("Save new Vehicle and verify database retrieval")
    void saveAndRetrieveVehicle() {
        Vehicle vehicle = new Vehicle(
                "TEST_VEH_01",
                100.0,
                28.7041,
                77.1025,
                "Karol Bagh, Delhi"
        );

        persistVehicle(vehicle);

        Optional<Vehicle> found = vehicleRepository.findById("TEST_VEH_01");
        assertTrue(found.isPresent());
        assertEquals("TEST_VEH_01", found.get().getVehicleId());
        assertEquals(100.0, found.get().getCapacity());
        assertEquals("Karol Bagh, Delhi", found.get().getCurrentAddress());
    }

    @Test
    @DisplayName("Upsert Vehicle with existing ID updates the existing record")
    void upsertVehicleUpdatesExisting() {
        Vehicle initial = new Vehicle(
                "TEST_VEH_UP",
                80.0,
                28.5355,
                77.3910,
                "Old Location, Noida"
        );
        persistVehicle(initial);

        // Same vehicleId, updated capacity and coordinates
        Vehicle updated = new Vehicle(
                "TEST_VEH_UP",
                120.0,
                28.4595,
                77.0266,
                "New Location, Gurgaon"
        );
        persistVehicle(updated);

        Optional<Vehicle> retrieved = vehicleRepository.findById("TEST_VEH_UP");
        assertTrue(retrieved.isPresent());
        assertEquals(120.0, retrieved.get().getCapacity());
        assertEquals("New Location, Gurgaon", retrieved.get().getCurrentAddress());
    }

    @Test
    @DisplayName("Batch save Vehicles through VehicleService persists all items")
    void batchSaveVehiclesThroughService() {
        List<VehicleRequest> list = new ArrayList<>();
        list.add(new VehicleRequest("BATCH_VEH_1", 100.0, 28.7041, 77.1025, "Karol Bagh"));
        list.add(new VehicleRequest("BATCH_VEH_2", 80.0, 28.5355, 77.3910, "Sector 18 Noida"));

        for (VehicleRequest r : list) {
            createdVehicleIds.add(r.getVehicleId());
        }

        VehicleBatchRequest batch = new VehicleBatchRequest(list);
        List<Vehicle> saved = vehicleService.saveVehicleBatch(batch);

        assertEquals(2, saved.size());
        assertTrue(vehicleRepository.findById("BATCH_VEH_1").isPresent());
        assertTrue(vehicleRepository.findById("BATCH_VEH_2").isPresent());
    }
}
