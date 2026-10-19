package com.chandan.freightfox.controller;

import com.chandan.freightfox.dto.ApiResponse;
import com.chandan.freightfox.dto.AssignedOrderDto;
import com.chandan.freightfox.dto.DispatchPlanResponse;
import com.chandan.freightfox.dto.OrderBatchRequest;
import com.chandan.freightfox.dto.OrderRequest;
import com.chandan.freightfox.dto.UnassignedOrderDto;
import com.chandan.freightfox.dto.VehicleBatchRequest;
import com.chandan.freightfox.dto.VehicleDispatchEntry;
import com.chandan.freightfox.dto.VehicleRequest;
import com.chandan.freightfox.repository.OrderRepository;
import com.chandan.freightfox.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration tests sending realistic HTTP requests over the wire to embedded server.
 * Verifies the full pipeline: HTTP -> Controller -> Service -> JPA/MySQL -> DispatchOptimizer -> Response DTOs.
 * Uses safe ID tracking to clean up only test-generated rows.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DispatchEndToEndIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

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

    @Test
    @DisplayName("End-to-End: POST vehicles, POST orders, then GET dispatch plan over HTTP")
    void testFullDispatchLifecycleOverHttp() {
        // 1. Submit vehicles
        List<VehicleRequest> vehicleList = List.of(
                new VehicleRequest("E2E_VEH_01", 100.0, 12.9716, 77.6413, "Indiranagar, Bangalore"),
                new VehicleRequest("E2E_VEH_02", 150.0, 13.0674, 80.2376, "T Nagar, Chennai")
        );
        for (VehicleRequest v : vehicleList) {
            createdVehicleIds.add(v.getVehicleId());
        }

        ResponseEntity<ApiResponse> vehicleResponse = restTemplate.postForEntity(
                "/api/dispatch/vehicles",
                new VehicleBatchRequest(vehicleList),
                ApiResponse.class
        );
        assertEquals(HttpStatus.OK, vehicleResponse.getStatusCode());
        assertNotNull(vehicleResponse.getBody());
        assertEquals("Vehicle details accepted.", vehicleResponse.getBody().getMessage());
        assertEquals("success", vehicleResponse.getBody().getStatus());

        // 2. Submit delivery orders
        List<OrderRequest> orderList = List.of(
                new OrderRequest("E2E_ORD_01", 12.9716, 77.5946, "MG Road, Bangalore", 10.0, "HIGH"),
                new OrderRequest("E2E_ORD_02", 13.0827, 80.2707, "Anna Salai, Chennai", 20.0, "MEDIUM"),
                new OrderRequest("E2E_ORD_03", 28.6139, 77.2090, "Delhi (Too Heavy)", 200.0, "LOW")
        );
        for (OrderRequest o : orderList) {
            createdOrderIds.add(o.getOrderId());
        }

        ResponseEntity<ApiResponse> orderResponse = restTemplate.postForEntity(
                "/api/dispatch/orders",
                new OrderBatchRequest(orderList),
                ApiResponse.class
        );
        assertEquals(HttpStatus.OK, orderResponse.getStatusCode());
        assertNotNull(orderResponse.getBody());
        assertEquals("Delivery orders accepted.", orderResponse.getBody().getMessage());
        assertEquals("success", orderResponse.getBody().getStatus());

        // 3. Retrieve dispatch plan
        ResponseEntity<DispatchPlanResponse> planResponse = restTemplate.getForEntity(
                "/api/dispatch/plan",
                DispatchPlanResponse.class
        );
        assertEquals(HttpStatus.OK, planResponse.getStatusCode());
        assertNotNull(planResponse.getBody());

        DispatchPlanResponse plan = planResponse.getBody();
        assertTrue(plan.getDispatchPlan().size() >= 2);

        // Verify distance string format "X.XX km"
        for (VehicleDispatchEntry entry : plan.getDispatchPlan()) {
            assertNotNull(entry.getTotalDistance());
            assertTrue(entry.getTotalDistance().matches("^\\d+\\.\\d{2} km$"),
                    "Distance should match X.XX km pattern: " + entry.getTotalDistance());
        }

        // Verify E2E_ORD_03 (200 kg) is in unassigned orders because no vehicle has 200 kg capacity
        boolean unassignedFound = plan.getUnassignedOrders().stream()
                .anyMatch(u -> "E2E_ORD_03".equals(u.getOrderId()));
        assertTrue(unassignedFound, "Oversized order E2E_ORD_03 should be in unassignedOrders");
    }

    @Test
    @DisplayName("End-to-End: Full 30-order, 5-vehicle sample dataset from PDF tested over REST endpoints")
    void testFullSampleDatasetOverHttp() {
        // Prepare 5 vehicles from PDF
        List<VehicleRequest> vehicles = List.of(
                new VehicleRequest("E2E_PDF_V01", 100.0, 28.7041, 77.1025, "Karol Bagh, Delhi, India"),
                new VehicleRequest("E2E_PDF_V02", 80.0, 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India"),
                new VehicleRequest("E2E_PDF_V03", 120.0, 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India"),
                new VehicleRequest("E2E_PDF_V04", 90.0, 28.6139, 77.2090, "Connaught Place, Delhi, India"),
                new VehicleRequest("E2E_PDF_V05", 110.0, 28.7041, 77.1025, "Karol Bagh, Delhi, India")
        );
        for (VehicleRequest v : vehicles) {
            createdVehicleIds.add(v.getVehicleId());
        }

        // Prepare 30 orders from PDF
        List<OrderRequest> orders = new ArrayList<>();
        orders.add(new OrderRequest("E2E_PDF_O01", 28.6139, 77.2090, "Connaught Place, Delhi, India", 15.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O02", 28.6139, 77.2090, "Connaught Place, Delhi, India", 10.0, "MEDIUM"));
        orders.add(new OrderRequest("E2E_PDF_O03", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O04", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 25.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O05", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 30.0, "MEDIUM"));
        orders.add(new OrderRequest("E2E_PDF_O06", 28.6139, 77.2090, "Connaught Place, Delhi, India", 40.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O07", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O08", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 25.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O09", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, "MEDIUM"));
        orders.add(new OrderRequest("E2E_PDF_O10", 28.6139, 77.2090, "Connaught Place, Delhi, India", 30.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O11", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O12", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 10.0, "MEDIUM"));
        orders.add(new OrderRequest("E2E_PDF_O13", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 25.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O14", 28.6139, 77.2090, "Connaught Place, Delhi, India", 15.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O15", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O16", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O17", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, "MEDIUM"));
        orders.add(new OrderRequest("E2E_PDF_O18", 28.6139, 77.2090, "Connaught Place, Delhi, India", 10.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O19", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O20", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O21", 28.6139, 77.2090, "Connaught Place, Delhi, India", 25.0, "MEDIUM"));
        orders.add(new OrderRequest("E2E_PDF_O22", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O23", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O24", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O25", 28.6139, 77.2090, "Connaught Place, Delhi, India", 20.0, "MEDIUM"));
        orders.add(new OrderRequest("E2E_PDF_O26", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 25.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O27", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 20.0, "MEDIUM"));
        orders.add(new OrderRequest("E2E_PDF_O28", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, "LOW"));
        orders.add(new OrderRequest("E2E_PDF_O29", 28.6139, 77.2090, "Connaught Place, Delhi, India", 30.0, "HIGH"));
        orders.add(new OrderRequest("E2E_PDF_O30", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, "LOW"));
        for (OrderRequest o : orders) {
            createdOrderIds.add(o.getOrderId());
        }

        // POST /api/dispatch/vehicles
        ResponseEntity<ApiResponse> vResp = restTemplate.postForEntity(
                "/api/dispatch/vehicles",
                new VehicleBatchRequest(vehicles),
                ApiResponse.class
        );
        assertEquals(HttpStatus.OK, vResp.getStatusCode());
        assertEquals("Vehicle details accepted.", vResp.getBody().getMessage());

        // POST /api/dispatch/orders
        ResponseEntity<ApiResponse> oResp = restTemplate.postForEntity(
                "/api/dispatch/orders",
                new OrderBatchRequest(orders),
                ApiResponse.class
        );
        assertEquals(HttpStatus.OK, oResp.getStatusCode());
        assertEquals("Delivery orders accepted.", oResp.getBody().getMessage());

        // GET /api/dispatch/plan
        ResponseEntity<DispatchPlanResponse> planResp = restTemplate.getForEntity(
                "/api/dispatch/plan",
                DispatchPlanResponse.class
        );
        assertEquals(HttpStatus.OK, planResp.getStatusCode());
        DispatchPlanResponse plan = planResp.getBody();
        assertNotNull(plan);

        // Verify all 30 orders are accounted for
        Set<String> allIds = new HashSet<>();
        for (VehicleDispatchEntry entry : plan.getDispatchPlan()) {
            for (AssignedOrderDto a : entry.getAssignedOrders()) {
                if (createdOrderIds.contains(a.getOrderId())) {
                    assertTrue(allIds.add(a.getOrderId()), "Duplicate assigned order: " + a.getOrderId());
                }
            }
        }
        for (UnassignedOrderDto u : plan.getUnassignedOrders()) {
            if (createdOrderIds.contains(u.getOrderId())) {
                assertTrue(allIds.add(u.getOrderId()), "Duplicate unassigned order: " + u.getOrderId());
            }
        }
        assertEquals(30, allIds.size(), "All 30 sample orders must be either assigned or unassigned");
    }
}
