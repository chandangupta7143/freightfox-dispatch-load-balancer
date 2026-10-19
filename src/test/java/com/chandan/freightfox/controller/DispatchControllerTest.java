package com.chandan.freightfox.controller;

import com.chandan.freightfox.dto.AssignedOrderDto;
import com.chandan.freightfox.dto.DispatchPlanResponse;
import com.chandan.freightfox.dto.UnassignedOrderDto;
import com.chandan.freightfox.dto.VehicleDispatchEntry;
import com.chandan.freightfox.exception.GlobalExceptionHandler;
import com.chandan.freightfox.service.DispatchService;
import com.chandan.freightfox.service.OrderService;
import com.chandan.freightfox.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DispatchController.class)
@Import(GlobalExceptionHandler.class)
class DispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private DispatchService dispatchService;

    // ─────────────────────────────────────────────────────────────────────────────
    // POST /api/dispatch/orders
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/dispatch/orders with valid orders returns 200 and success message")
    void postOrdersSuccess() throws Exception {
        String json = """
                {
                  "orders": [
                    {
                      "orderId": "ORD001",
                      "latitude": 12.9716,
                      "longitude": 77.5946,
                      "address": "MG Road, Bangalore, Karnataka, India",
                      "packageWeight": 10.0,
                      "priority": "HIGH"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Delivery orders accepted.")));

        verify(orderService).saveOrderBatch(any());
    }

    @Test
    @DisplayName("POST /api/dispatch/orders with invalid field returns 400")
    void postOrdersInvalidFieldReturns400() throws Exception {
        // Missing latitude, negative weight, invalid priority
        String json = """
                {
                  "orders": [
                    {
                      "orderId": "ORD001",
                      "latitude": 95.0,
                      "longitude": 77.5946,
                      "address": "MG Road, Bangalore",
                      "packageWeight": -5.0,
                      "priority": "INVALID"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    @DisplayName("POST /api/dispatch/orders with duplicate orderId in batch returns 400")
    void postOrdersDuplicateIdReturns400() throws Exception {
        String json = """
                {
                  "orders": [
                    {
                      "orderId": "ORD001",
                      "latitude": 12.9716,
                      "longitude": 77.5946,
                      "address": "MG Road, Bangalore",
                      "packageWeight": 10.0,
                      "priority": "HIGH"
                    },
                    {
                      "orderId": "ORD001",
                      "latitude": 13.0827,
                      "longitude": 80.2707,
                      "address": "Anna Salai, Chennai",
                      "packageWeight": 20.0,
                      "priority": "MEDIUM"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("POST /api/dispatch/orders with empty batch returns 400")
    void postOrdersEmptyBatchReturns400() throws Exception {
        String json = """
                {
                  "orders": []
                }
                """;

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // POST /api/dispatch/vehicles
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/dispatch/vehicles with valid vehicles returns 200 and success message")
    void postVehiclesSuccess() throws Exception {
        String json = """
                {
                  "vehicles": [
                    {
                      "vehicleId": "VEH001",
                      "capacity": 100.0,
                      "currentLatitude": 12.9716,
                      "currentLongitude": 77.6413,
                      "currentAddress": "Indiranagar, Bangalore, Karnataka, India"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Vehicle details accepted.")));

        verify(vehicleService).saveVehicleBatch(any());
    }

    @Test
    @DisplayName("POST /api/dispatch/vehicles with invalid capacity returns 400")
    void postVehiclesInvalidCapacityReturns400() throws Exception {
        String json = """
                {
                  "vehicles": [
                    {
                      "vehicleId": "VEH001",
                      "capacity": -10.0,
                      "currentLatitude": 12.9716,
                      "currentLongitude": 77.6413,
                      "currentAddress": "Indiranagar, Bangalore"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("POST /api/dispatch/vehicles with duplicate vehicleId in batch returns 400")
    void postVehiclesDuplicateIdReturns400() throws Exception {
        String json = """
                {
                  "vehicles": [
                    {
                      "vehicleId": "VEH001",
                      "capacity": 100.0,
                      "currentLatitude": 12.9716,
                      "currentLongitude": 77.6413,
                      "currentAddress": "Indiranagar"
                    },
                    {
                      "vehicleId": "VEH001",
                      "capacity": 150.0,
                      "currentLatitude": 13.0674,
                      "currentLongitude": 80.2376,
                      "currentAddress": "T Nagar"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GET /api/dispatch/plan
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/dispatch/plan returns 200 with populated plan and unassigned orders")
    void getPlanPopulatedReturns200() throws Exception {
        List<AssignedOrderDto> assignedOrders = List.of(
                new AssignedOrderDto("ORD001", 12.9716, 77.5946, "MG Road, Bangalore", 10.0, "HIGH")
        );
        VehicleDispatchEntry entry = new VehicleDispatchEntry("VEH001", 10.0, "5.23 km", assignedOrders);
        List<UnassignedOrderDto> unassigned = List.of(
                new UnassignedOrderDto("ORD006", "Insufficient vehicle capacity available")
        );

        DispatchPlanResponse response = new DispatchPlanResponse(List.of(entry), unassigned);
        when(dispatchService.generateDispatchPlan()).thenReturn(response);

        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dispatchPlan", hasSize(1)))
                .andExpect(jsonPath("$.dispatchPlan[0].vehicleId", is("VEH001")))
                .andExpect(jsonPath("$.dispatchPlan[0].totalLoad", is(10.0)))
                .andExpect(jsonPath("$.dispatchPlan[0].totalDistance", is("5.23 km")))
                .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders", hasSize(1)))
                .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders[0].orderId", is("ORD001")))
                .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders[0].priority", is("HIGH")))
                .andExpect(jsonPath("$.unassignedOrders", hasSize(1)))
                .andExpect(jsonPath("$.unassignedOrders[0].orderId", is("ORD006")))
                .andExpect(jsonPath("$.unassignedOrders[0].reason", containsString("capacity")));
    }

    @Test
    @DisplayName("GET /api/dispatch/plan with no data returns 200 with empty arrays")
    void getPlanEmptyDataReturns200() throws Exception {
        DispatchPlanResponse emptyResponse = new DispatchPlanResponse(Collections.emptyList(), Collections.emptyList());
        when(dispatchService.generateDispatchPlan()).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dispatchPlan", hasSize(0)))
                .andExpect(jsonPath("$.unassignedOrders", hasSize(0)));
    }

    @Test
    @DisplayName("Malformed JSON returns 400 Bad Request")
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ malformed_json: true "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")));
    }
}
