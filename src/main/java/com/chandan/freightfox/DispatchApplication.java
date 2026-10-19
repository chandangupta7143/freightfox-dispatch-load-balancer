package com.chandan.freightfox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FreightFox Dispatch Load Balancer
 *
 * Spring Boot application entry point.
 * Optimises delivery order assignment to vehicles based on:
 *   - Priority: HIGH orders processed before MEDIUM and LOW.
 *   - Capacity: total assigned weight must not exceed vehicle capacity.
 *   - Distance: orders assigned to minimise total travel distance (Haversine formula).
 */
@SpringBootApplication
public class DispatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatchApplication.class, args);
    }
}
