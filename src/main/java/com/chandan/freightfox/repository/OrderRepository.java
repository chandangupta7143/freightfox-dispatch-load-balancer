package com.chandan.freightfox.repository;

import com.chandan.freightfox.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for DeliveryOrder entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<DeliveryOrder, String> {
}
