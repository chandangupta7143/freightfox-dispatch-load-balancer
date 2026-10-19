package com.chandan.freightfox.service;

import com.chandan.freightfox.dto.VehicleBatchRequest;
import com.chandan.freightfox.dto.VehicleRequest;
import com.chandan.freightfox.entity.Vehicle;
import com.chandan.freightfox.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service managing vehicle persistence and retrieval.
 */
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Persists a batch of vehicles within a single transaction.
     * Enforces in-memory uniqueness of vehicleId within the batch before any DB write.
     * Uses JPA saveAll() (em.merge under the hood):
     * - New vehicle IDs are inserted.
     * - Existing vehicle IDs are updated.
     *
     * @param batchRequest validated batch input
     * @return list of saved Vehicle entities
     */
    @Transactional
    public List<Vehicle> saveVehicleBatch(VehicleBatchRequest batchRequest) {
        if (batchRequest == null || batchRequest.getVehicles() == null || batchRequest.getVehicles().isEmpty()) {
            throw new IllegalArgumentException("Vehicles batch must not be empty");
        }

        List<VehicleRequest> requests = batchRequest.getVehicles();
        Set<String> seenIds = new HashSet<>();
        List<Vehicle> entities = new ArrayList<>(requests.size());

        for (VehicleRequest req : requests) {
            String trimmedId = req.getVehicleId() != null ? req.getVehicleId().trim() : null;
            if (trimmedId == null || trimmedId.isEmpty()) {
                throw new IllegalArgumentException("vehicleId must not be blank");
            }
            if (!seenIds.add(trimmedId)) {
                throw new IllegalArgumentException("Duplicate vehicleId found in incoming batch: " + trimmedId);
            }

            Vehicle vehicle = new Vehicle(
                    trimmedId,
                    req.getCapacity(),
                    req.getCurrentLatitude(),
                    req.getCurrentLongitude(),
                    req.getCurrentAddress()
            );
            entities.add(vehicle);
        }

        return vehicleRepository.saveAll(entities);
    }

    /**
     * Retrieves all vehicles currently stored.
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}
