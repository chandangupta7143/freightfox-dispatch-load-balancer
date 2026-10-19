package com.chandan.freightfox.controller;

import com.chandan.freightfox.dto.ApiResponse;
import com.chandan.freightfox.dto.DispatchPlanResponse;
import com.chandan.freightfox.dto.ErrorResponse;
import com.chandan.freightfox.dto.OrderBatchRequest;
import com.chandan.freightfox.dto.VehicleBatchRequest;
import com.chandan.freightfox.service.DispatchService;
import com.chandan.freightfox.service.OrderService;
import com.chandan.freightfox.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Primary REST controller providing the three dispatch API endpoints:
 * 1. POST /api/dispatch/orders - Submit batch delivery orders
 * 2. POST /api/dispatch/vehicles - Submit batch fleet vehicles
 * 3. GET /api/dispatch/plan - Retrieve optimized dispatch plan
 */
@RestController
@RequestMapping("/api/dispatch")
@Tag(name = "Dispatch Load Balancer", description = "Endpoints for managing delivery orders, fleet vehicles, and generating load-balanced dispatch plans")
public class DispatchController {

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final DispatchService dispatchService;

    public DispatchController(OrderService orderService, VehicleService vehicleService, DispatchService dispatchService) {
        this.orderService = orderService;
        this.vehicleService = vehicleService;
        this.dispatchService = dispatchService;
    }

    @PostMapping("/orders")
    @Operation(summary = "Input Delivery Orders", description = "Accepts a batch of delivery orders, validates coordinates, weights, priorities, and persists them via transactional upsert.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Delivery orders accepted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or duplicate IDs in batch",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse> inputOrders(@Valid @RequestBody OrderBatchRequest request) {
        orderService.saveOrderBatch(request);
        return ResponseEntity.ok(ApiResponse.success("Delivery orders accepted."));
    }

    @PostMapping("/vehicles")
    @Operation(summary = "Input Fleet Details", description = "Accepts a batch of fleet vehicles, validates capacities and coordinates, and persists them via transactional upsert.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle details accepted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or duplicate IDs in batch",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse> inputVehicles(@Valid @RequestBody VehicleBatchRequest request) {
        vehicleService.saveVehicleBatch(request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle details accepted."));
    }

    @GetMapping("/plan")
    @Operation(summary = "Retrieve Dispatch Plan", description = "Optimizes order assignments across available vehicles respecting capacity constraints, prioritizing urgent orders (HIGH > MEDIUM > LOW), and minimizing Haversine travel distance.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dispatch plan generated successfully",
                    content = @Content(schema = @Schema(implementation = DispatchPlanResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DispatchPlanResponse> getDispatchPlan() {
        DispatchPlanResponse response = dispatchService.generateDispatchPlan();
        return ResponseEntity.ok(response);
    }
}
