package com.chandan.freightfox.repository;

import com.chandan.freightfox.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Vehicle entities.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
}
