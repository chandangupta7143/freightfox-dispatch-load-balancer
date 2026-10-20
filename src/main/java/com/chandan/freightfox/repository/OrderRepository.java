package com.chandan.freightfox.repository;

import com.chandan.freightfox.entity.DeliveryOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for DeliveryOrder documents.
 */
@Repository
public interface OrderRepository extends MongoRepository<DeliveryOrder, String> {
}
