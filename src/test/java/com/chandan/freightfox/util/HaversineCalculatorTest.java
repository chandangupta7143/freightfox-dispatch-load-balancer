package com.chandan.freightfox.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HaversineCalculatorTest {

    @Test
    @DisplayName("Identical coordinates return exactly zero distance")
    void identicalCoordinatesReturnZero() {
        double dist1 = HaversineCalculator.calculateDistance(28.6139, 77.2090, 28.6139, 77.2090);
        assertEquals(0.0, dist1, 1e-9);

        double dist2 = HaversineCalculator.calculateDistance(0.0, 0.0, 0.0, 0.0);
        assertEquals(0.0, dist2, 1e-9);

        double dist3 = HaversineCalculator.calculateDistance(-33.8688, 151.2093, -33.8688, 151.2093);
        assertEquals(0.0, dist3, 1e-9);
    }

    @Test
    @DisplayName("Known coordinate pair produces plausible distance (Bangalore to Chennai ~290 km)")
    void knownDistanceBangaloreToChennai() {
        // Bangalore MG Road (12.9716, 77.5946) to Chennai Anna Salai (13.0827, 80.2707)
        double distance = HaversineCalculator.calculateDistance(12.9716, 77.5946, 13.0827, 80.2707);
        // Expected great-circle distance is approximately 290.5 km
        assertTrue(distance > 285.0 && distance < 295.0, "Expected ~290.5 km but got " + distance);
    }

    @Test
    @DisplayName("Known coordinate pair produces plausible distance (Connaught Place to Noida Sec 18 ~20.5 km)")
    void knownDistanceDelhiToNoida() {
        // Connaught Place (28.6139, 77.2090) to Sector 18 Noida (28.5355, 77.3910)
        double distance = HaversineCalculator.calculateDistance(28.6139, 77.2090, 28.5355, 77.3910);
        assertTrue(distance > 18.0 && distance < 23.0, "Expected ~20.5 km but got " + distance);
    }

    @Test
    @DisplayName("One degree of latitude along meridian is approximately 111.2 km")
    void oneDegreeLatitudeDistance() {
        double distance = HaversineCalculator.calculateDistance(0.0, 0.0, 1.0, 0.0);
        // Earth circumference / 360 = 2 * PI * 6371 / 360 ≈ 111.195 km
        assertEquals(111.195, distance, 0.1);
    }

    @Test
    @DisplayName("Results are always non-negative")
    void resultsAlwaysNonNegative() {
        double d1 = HaversineCalculator.calculateDistance(-45.0, -120.0, 45.0, 120.0);
        assertTrue(d1 >= 0.0);

        double d2 = HaversineCalculator.calculateDistance(90.0, 0.0, -90.0, 0.0);
        assertTrue(d2 >= 0.0);
    }

    @Test
    @DisplayName("Valid boundary coordinates -90, 90, -180, 180 are handled without error")
    void boundaryCoordinatesHandled() {
        // North Pole to South Pole
        double poleToPole = HaversineCalculator.calculateDistance(90.0, 0.0, -90.0, 0.0);
        // Half circumference: PI * 6371 ≈ 20015 km
        assertEquals(Math.PI * 6371.0, poleToPole, 1.0);

        // Date line crossing
        double dateLine = HaversineCalculator.calculateDistance(0.0, -180.0, 0.0, 180.0);
        assertEquals(0.0, dateLine, 1e-6);
    }

    @Test
    @DisplayName("Invalid latitude coordinates throw IllegalArgumentException")
    void invalidLatitudeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> HaversineCalculator.calculateDistance(90.1, 0.0, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> HaversineCalculator.calculateDistance(-90.1, 0.0, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> HaversineCalculator.calculateDistance(0.0, 0.0, 95.0, 0.0));
    }

    @Test
    @DisplayName("Invalid longitude coordinates throw IllegalArgumentException")
    void invalidLongitudeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> HaversineCalculator.calculateDistance(0.0, 180.1, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> HaversineCalculator.calculateDistance(0.0, -180.5, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> HaversineCalculator.calculateDistance(0.0, 0.0, 0.0, -200.0));
    }
}
