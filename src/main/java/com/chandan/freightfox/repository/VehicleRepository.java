package com.chandan.freightfox.repository;

import com.chandan.freightfox.entity.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for Vehicle documents.
 */
@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
}
