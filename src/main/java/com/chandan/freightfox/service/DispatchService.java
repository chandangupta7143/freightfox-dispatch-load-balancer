package com.chandan.freightfox.service;

import com.chandan.freightfox.algorithm.DispatchOptimizer;
import com.chandan.freightfox.dto.AssignedOrderDto;
import com.chandan.freightfox.dto.DispatchPlanResponse;
import com.chandan.freightfox.dto.UnassignedOrderDto;
import com.chandan.freightfox.dto.VehicleDispatchEntry;
import com.chandan.freightfox.entity.DeliveryOrder;
import com.chandan.freightfox.entity.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Service orchestrating the retrieval of orders and vehicles,
 * execution of dispatch optimization, and transformation into the response contract.
 */
@Service
public class DispatchService {

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final DispatchOptimizer dispatchOptimizer;

    public DispatchService(OrderService orderService, VehicleService vehicleService, DispatchOptimizer dispatchOptimizer) {
        this.orderService = orderService;
        this.vehicleService = vehicleService;
        this.dispatchOptimizer = dispatchOptimizer;
    }

    /**
     * Generates the optimized dispatch plan from currently stored orders and fleet vehicles.
     *
     * @return DispatchPlanResponse containing vehicle routes and unassigned orders
     */
    @Transactional(readOnly = true)
    public DispatchPlanResponse generateDispatchPlan() {
        List<DeliveryOrder> orders = orderService.getAllOrders();
        List<Vehicle> vehicles = vehicleService.getAllVehicles();

        DispatchOptimizer.OptimizationResult result = dispatchOptimizer.optimize(orders, vehicles);

        List<VehicleDispatchEntry> planEntries = new ArrayList<>();
        for (DispatchOptimizer.VehicleAssignment va : result.getVehicleAssignments()) {
            List<AssignedOrderDto> orderDtos = new ArrayList<>();
            for (DeliveryOrder o : va.getAssignedOrders()) {
                orderDtos.add(new AssignedOrderDto(
                        o.getOrderId(),
                        o.getLatitude(),
                        o.getLongitude(),
                        o.getAddress(),
                        o.getPackageWeight(),
                        o.getPriority() != null ? o.getPriority().name() : null
                ));
            }

            String formattedDistance = String.format(Locale.US, "%.2f km", va.getTotalDistance());

            planEntries.add(new VehicleDispatchEntry(
                    va.getVehicleId(),
                    va.getTotalLoad(),
                    formattedDistance,
                    orderDtos
            ));
        }

        List<UnassignedOrderDto> unassignedDtos = new ArrayList<>();
        for (DispatchOptimizer.UnassignedOrder uo : result.getUnassignedOrders()) {
            unassignedDtos.add(new UnassignedOrderDto(
                    uo.getOrderId(),
                    uo.getReason()
            ));
        }

        return new DispatchPlanResponse(planEntries, unassignedDtos);
    }
}
