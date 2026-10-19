package com.chandan.freightfox.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // OrderRequest Validation Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid OrderRequest passes validation")
    void validOrderRequestPasses() {
        OrderRequest request = new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road, Bangalore", 10.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Expected no violations for valid order");
    }

    @Test
    @DisplayName("Blank orderId fails validation")
    void blankOrderIdFails() {
        OrderRequest request = new OrderRequest("   ", 12.9716, 77.5946, "MG Road, Bangalore", 10.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orderId")));
    }

    @Test
    @DisplayName("Null orderId fails validation")
    void nullOrderIdFails() {
        OrderRequest request = new OrderRequest(null, 12.9716, 77.5946, "MG Road, Bangalore", 10.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orderId")));
    }

    @Test
    @DisplayName("Invalid latitude outside [-90, 90] fails validation")
    void invalidLatitudeFails() {
        OrderRequest highLat = new OrderRequest("ORD001", 90.1, 77.5946, "MG Road, Bangalore", 10.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> vHigh = validator.validate(highLat);
        assertFalse(vHigh.isEmpty());
        assertTrue(vHigh.stream().anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));

        OrderRequest lowLat = new OrderRequest("ORD001", -90.5, 77.5946, "MG Road, Bangalore", 10.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> vLow = validator.validate(lowLat);
        assertFalse(vLow.isEmpty());
        assertTrue(vLow.stream().anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }

    @Test
    @DisplayName("Invalid longitude outside [-180, 180] fails validation")
    void invalidLongitudeFails() {
        OrderRequest highLon = new OrderRequest("ORD001", 12.9716, 180.5, "MG Road, Bangalore", 10.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> vHigh = validator.validate(highLon);
        assertFalse(vHigh.isEmpty());
        assertTrue(vHigh.stream().anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));

        OrderRequest lowLon = new OrderRequest("ORD001", 12.9716, -181.0, "MG Road, Bangalore", 10.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> vLow = validator.validate(lowLon);
        assertFalse(vLow.isEmpty());
        assertTrue(vLow.stream().anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }

    @Test
    @DisplayName("Blank address fails validation")
    void blankAddressFails() {
        OrderRequest request = new OrderRequest("ORD001", 12.9716, 77.5946, "  ", 10.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
    }

    @Test
    @DisplayName("Zero or negative packageWeight fails validation")
    void invalidWeightFails() {
        OrderRequest zeroWeight = new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road, Bangalore", 0.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> vZero = validator.validate(zeroWeight);
        assertFalse(vZero.isEmpty());
        assertTrue(vZero.stream().anyMatch(v -> v.getPropertyPath().toString().equals("packageWeight")));

        OrderRequest negWeight = new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road, Bangalore", -5.0, "HIGH");
        Set<ConstraintViolation<OrderRequest>> vNeg = validator.validate(negWeight);
        assertFalse(vNeg.isEmpty());
        assertTrue(vNeg.stream().anyMatch(v -> v.getPropertyPath().toString().equals("packageWeight")));
    }

    @Test
    @DisplayName("Invalid priority string fails validation")
    void invalidPriorityFails() {
        OrderRequest request = new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road, Bangalore", 10.0, "URGENT");
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("priority")));
    }

    @Test
    @DisplayName("Valid priorities HIGH, MEDIUM, LOW pass validation")
    void validPrioritiesPass() {
        for (String p : List.of("HIGH", "MEDIUM", "LOW", "high", "medium", "low")) {
            OrderRequest req = new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road, Bangalore", 10.0, p);
            Set<ConstraintViolation<OrderRequest>> violations = validator.validate(req);
            assertTrue(violations.isEmpty(), "Priority " + p + " should be valid");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // VehicleRequest Validation Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid VehicleRequest passes validation")
    void validVehicleRequestPasses() {
        VehicleRequest request = new VehicleRequest("VEH001", 100.0, 12.9716, 77.6413, "Indiranagar, Bangalore");
        Set<ConstraintViolation<VehicleRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Expected no violations for valid vehicle");
    }

    @Test
    @DisplayName("Blank vehicleId fails validation")
    void blankVehicleIdFails() {
        VehicleRequest request = new VehicleRequest("", 100.0, 12.9716, 77.6413, "Indiranagar, Bangalore");
        Set<ConstraintViolation<VehicleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("vehicleId")));
    }

    @Test
    @DisplayName("Zero or negative capacity fails validation")
    void invalidCapacityFails() {
        VehicleRequest zeroCap = new VehicleRequest("VEH001", 0.0, 12.9716, 77.6413, "Indiranagar, Bangalore");
        Set<ConstraintViolation<VehicleRequest>> vZero = validator.validate(zeroCap);
        assertFalse(vZero.isEmpty());
        assertTrue(vZero.stream().anyMatch(v -> v.getPropertyPath().toString().equals("capacity")));

        VehicleRequest negCap = new VehicleRequest("VEH001", -50.0, 12.9716, 77.6413, "Indiranagar, Bangalore");
        Set<ConstraintViolation<VehicleRequest>> vNeg = validator.validate(negCap);
        assertFalse(vNeg.isEmpty());
        assertTrue(vNeg.stream().anyMatch(v -> v.getPropertyPath().toString().equals("capacity")));
    }

    @Test
    @DisplayName("Invalid vehicle coordinates fail validation")
    void invalidVehicleCoordsFail() {
        VehicleRequest invalidLat = new VehicleRequest("VEH001", 100.0, 95.0, 77.6413, "Indiranagar, Bangalore");
        Set<ConstraintViolation<VehicleRequest>> vLat = validator.validate(invalidLat);
        assertFalse(vLat.isEmpty());
        assertTrue(vLat.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currentLatitude")));

        VehicleRequest invalidLon = new VehicleRequest("VEH001", 100.0, 12.9716, -185.0, "Indiranagar, Bangalore");
        Set<ConstraintViolation<VehicleRequest>> vLon = validator.validate(invalidLon);
        assertFalse(vLon.isEmpty());
        assertTrue(vLon.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currentLongitude")));
    }

    @Test
    @DisplayName("Blank currentAddress fails validation")
    void blankCurrentAddressFails() {
        VehicleRequest request = new VehicleRequest("VEH001", 100.0, 12.9716, 77.6413, "   ");
        Set<ConstraintViolation<VehicleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currentAddress")));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Batch Validation & Duplicate ID Detection Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Empty OrderBatchRequest fails validation")
    void emptyOrderBatchFails() {
        OrderBatchRequest batch = new OrderBatchRequest(Collections.emptyList());
        Set<ConstraintViolation<OrderBatchRequest>> violations = validator.validate(batch);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orders")));
    }

    @Test
    @DisplayName("Duplicate orderId in batch fails validation")
    void duplicateOrderIdInBatchFails() {
        List<OrderRequest> list = new ArrayList<>();
        list.add(new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road", 10.0, "HIGH"));
        list.add(new OrderRequest("ORD001", 13.0827, 80.2707, "Anna Salai", 20.0, "MEDIUM"));

        OrderBatchRequest batch = new OrderBatchRequest(list);
        Set<ConstraintViolation<OrderBatchRequest>> violations = validator.validate(batch);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Duplicate orderId")));
    }

    @Test
    @DisplayName("Unique orderIds in batch pass validation")
    void uniqueOrderIdsInBatchPass() {
        List<OrderRequest> list = new ArrayList<>();
        list.add(new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road", 10.0, "HIGH"));
        list.add(new OrderRequest("ORD002", 13.0827, 80.2707, "Anna Salai", 20.0, "MEDIUM"));

        OrderBatchRequest batch = new OrderBatchRequest(list);
        Set<ConstraintViolation<OrderBatchRequest>> violations = validator.validate(batch);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Empty VehicleBatchRequest fails validation")
    void emptyVehicleBatchFails() {
        VehicleBatchRequest batch = new VehicleBatchRequest(Collections.emptyList());
        Set<ConstraintViolation<VehicleBatchRequest>> violations = validator.validate(batch);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("vehicles")));
    }

    @Test
    @DisplayName("Duplicate vehicleId in batch fails validation")
    void duplicateVehicleIdInBatchFails() {
        List<VehicleRequest> list = new ArrayList<>();
        list.add(new VehicleRequest("VEH001", 100.0, 12.9716, 77.6413, "Indiranagar"));
        list.add(new VehicleRequest("VEH001", 150.0, 13.0674, 80.2376, "T Nagar"));

        VehicleBatchRequest batch = new VehicleBatchRequest(list);
        Set<ConstraintViolation<VehicleBatchRequest>> violations = validator.validate(batch);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Duplicate vehicleId")));
    }

    @Test
    @DisplayName("Unique vehicleIds in batch pass validation")
    void uniqueVehicleIdsInBatchPass() {
        List<VehicleRequest> list = new ArrayList<>();
        list.add(new VehicleRequest("VEH001", 100.0, 12.9716, 77.6413, "Indiranagar"));
        list.add(new VehicleRequest("VEH002", 150.0, 13.0674, 80.2376, "T Nagar"));

        VehicleBatchRequest batch = new VehicleBatchRequest(list);
        Set<ConstraintViolation<VehicleBatchRequest>> violations = validator.validate(batch);
        assertTrue(violations.isEmpty());
    }
}
