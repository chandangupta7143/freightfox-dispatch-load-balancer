package com.chandan.freightfox.service;

import com.chandan.freightfox.dto.OrderBatchRequest;
import com.chandan.freightfox.dto.OrderRequest;
import com.chandan.freightfox.entity.DeliveryOrder;
import com.chandan.freightfox.entity.OrderPriority;
import com.chandan.freightfox.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service managing delivery order persistence and retrieval in MongoDB.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Persists a batch of delivery orders.
     * Enforces in-memory uniqueness of orderId within the batch before any DB write.
     * Uses MongoRepository saveAll():
     * - New order IDs are inserted.
     * - Existing order IDs are updated (in-place replacement by matching _id).
     *
     * @param batchRequest validated batch input
     * @return list of saved DeliveryOrder entities
     */
    public List<DeliveryOrder> saveOrderBatch(OrderBatchRequest batchRequest) {
        if (batchRequest == null || batchRequest.getOrders() == null || batchRequest.getOrders().isEmpty()) {
            throw new IllegalArgumentException("Orders batch must not be empty");
        }

        List<OrderRequest> requests = batchRequest.getOrders();
        Set<String> seenIds = new HashSet<>();
        List<DeliveryOrder> entities = new ArrayList<>(requests.size());

        for (OrderRequest req : requests) {
            String trimmedId = req.getOrderId() != null ? req.getOrderId().trim() : null;
            if (trimmedId == null || trimmedId.isEmpty()) {
                throw new IllegalArgumentException("orderId must not be blank");
            }
            if (!seenIds.add(trimmedId)) {
                throw new IllegalArgumentException("Duplicate orderId found in incoming batch: " + trimmedId);
            }

            OrderPriority priority = OrderPriority.fromString(req.getPriority());
            if (priority == null) {
                throw new IllegalArgumentException("Invalid priority value: " + req.getPriority() + ". Expected HIGH, MEDIUM, or LOW");
            }

            DeliveryOrder order = new DeliveryOrder(
                    trimmedId,
                    req.getLatitude(),
                    req.getLongitude(),
                    req.getAddress(),
                    req.getPackageWeight(),
                    priority
            );
            entities.add(order);
        }

        return orderRepository.saveAll(entities);
    }

    /**
     * Retrieves all delivery orders currently stored.
     */
    public List<DeliveryOrder> getAllOrders() {
        return orderRepository.findAll();
    }
}
