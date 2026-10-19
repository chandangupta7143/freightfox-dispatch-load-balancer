package com.chandan.freightfox.util;

/**
 * Utility for calculating great-circle distance between two geographic coordinates
 * using the Haversine formula.
 *
 * Independent of Spring and database dependencies.
 */
public final class HaversineCalculator {

    /** Mean radius of the Earth in kilometres. */
    public static final double EARTH_RADIUS_KM = 6371.0;

    private static final double EPSILON = 1e-9;

    private HaversineCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculates the great-circle distance between two latitude/longitude points in kilometres.
     *
     * @param lat1 latitude of point 1 in degrees [-90.0, 90.0]
     * @param lon1 longitude of point 1 in degrees [-180.0, 180.0]
     * @param lat2 latitude of point 2 in degrees [-90.0, 90.0]
     * @param lon2 longitude of point 2 in degrees [-180.0, 180.0]
     * @return unrounded distance in kilometres (non-negative)
     * @throws IllegalArgumentException if any coordinate is outside valid geographical boundaries
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        validateCoordinates(lat1, lon1);
        validateCoordinates(lat2, lon2);

        // Identical coordinates check
        if (Math.abs(lat1 - lat2) < EPSILON && Math.abs(lon1 - lon2) < EPSILON) {
            return 0.0;
        }

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double sinDeltaPhi2 = Math.sin(deltaPhi / 2.0);
        double sinDeltaLambda2 = Math.sin(deltaLambda / 2.0);

        double a = sinDeltaPhi2 * sinDeltaPhi2
                + Math.cos(phi1) * Math.cos(phi2) * sinDeltaLambda2 * sinDeltaLambda2;

        // Clamp 'a' to [0.0, 1.0] to guard against floating-point precision errors
        a = Math.max(0.0, Math.min(1.0, a));

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_KM * c;
    }

    private static void validateCoordinates(double lat, double lon) {
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90.0 and 90.0. Provided: " + lat);
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180.0 and 180.0. Provided: " + lon);
        }
    }
}
