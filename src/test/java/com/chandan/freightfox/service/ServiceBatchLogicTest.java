package com.chandan.freightfox.service;

import com.chandan.freightfox.dto.OrderBatchRequest;
import com.chandan.freightfox.dto.OrderRequest;
import com.chandan.freightfox.dto.VehicleBatchRequest;
import com.chandan.freightfox.dto.VehicleRequest;
import com.chandan.freightfox.entity.DeliveryOrder;
import com.chandan.freightfox.entity.Vehicle;
import com.chandan.freightfox.repository.OrderRepository;
import com.chandan.freightfox.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceBatchLogicTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private OrderService orderService;

    @InjectMocks
    private VehicleService vehicleService;

    // ─────────────────────────────────────────────────────────────────────────────
    // OrderService Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OrderService throws IllegalArgumentException on empty batch")
    void orderServiceEmptyBatchThrows() {
        assertThrows(IllegalArgumentException.class, () -> orderService.saveOrderBatch(new OrderBatchRequest(Collections.emptyList())));
        assertThrows(IllegalArgumentException.class, () -> orderService.saveOrderBatch(null));
    }

    @Test
    @DisplayName("OrderService throws IllegalArgumentException on duplicate orderId")
    void orderServiceDuplicateOrderIdThrows() {
        List<OrderRequest> list = new ArrayList<>();
        list.add(new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road", 10.0, "HIGH"));
        list.add(new OrderRequest("ORD001", 13.0827, 80.2707, "Anna Salai", 20.0, "MEDIUM"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOrderBatch(new OrderBatchRequest(list)));
        assertEquals("Duplicate orderId found in incoming batch: ORD001", ex.getMessage());
    }

    @Test
    @DisplayName("OrderService throws IllegalArgumentException on blank orderId")
    void orderServiceBlankOrderIdThrows() {
        List<OrderRequest> list = new ArrayList<>();
        list.add(new OrderRequest("  ", 12.9716, 77.5946, "MG Road", 10.0, "HIGH"));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOrderBatch(new OrderBatchRequest(list)));
    }

    @Test
    @DisplayName("OrderService delegates valid orders to repository saveAll")
    void orderServiceDelegatesToSaveAll() {
        List<OrderRequest> list = new ArrayList<>();
        list.add(new OrderRequest("ORD001", 12.9716, 77.5946, "MG Road", 10.0, "HIGH"));
        list.add(new OrderRequest("ORD002", 13.0827, 80.2707, "Anna Salai", 20.0, "MEDIUM"));

        when(orderRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<DeliveryOrder> result = orderService.saveOrderBatch(new OrderBatchRequest(list));
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).saveAll(anyList());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // VehicleService Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("VehicleService throws IllegalArgumentException on empty batch")
    void vehicleServiceEmptyBatchThrows() {
        assertThrows(IllegalArgumentException.class, () -> vehicleService.saveVehicleBatch(new VehicleBatchRequest(Collections.emptyList())));
        assertThrows(IllegalArgumentException.class, () -> vehicleService.saveVehicleBatch(null));
    }

    @Test
    @DisplayName("VehicleService throws IllegalArgumentException on duplicate vehicleId")
    void vehicleServiceDuplicateVehicleIdThrows() {
        List<VehicleRequest> list = new ArrayList<>();
        list.add(new VehicleRequest("VEH001", 100.0, 12.9716, 77.6413, "Indiranagar"));
        list.add(new VehicleRequest("VEH001", 150.0, 13.0674, 80.2376, "T Nagar"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.saveVehicleBatch(new VehicleBatchRequest(list)));
        assertEquals("Duplicate vehicleId found in incoming batch: VEH001", ex.getMessage());
    }

    @Test
    @DisplayName("VehicleService throws IllegalArgumentException on blank vehicleId")
    void vehicleServiceBlankVehicleIdThrows() {
        List<VehicleRequest> list = new ArrayList<>();
        list.add(new VehicleRequest("  ", 100.0, 12.9716, 77.6413, "Indiranagar"));

        assertThrows(IllegalArgumentException.class,
                () -> vehicleService.saveVehicleBatch(new VehicleBatchRequest(list)));
    }

    @Test
    @DisplayName("VehicleService delegates valid vehicles to repository saveAll")
    void vehicleServiceDelegatesToSaveAll() {
        List<VehicleRequest> list = new ArrayList<>();
        list.add(new VehicleRequest("VEH001", 100.0, 12.9716, 77.6413, "Indiranagar"));
        list.add(new VehicleRequest("VEH002", 150.0, 13.0674, 80.2376, "T Nagar"));

        when(vehicleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Vehicle> result = vehicleService.saveVehicleBatch(new VehicleBatchRequest(list));
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(vehicleRepository).saveAll(anyList());
    }
}
