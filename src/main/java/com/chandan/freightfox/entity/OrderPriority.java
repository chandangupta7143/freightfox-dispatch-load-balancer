package com.chandan.freightfox.entity;

/**
 * Priority levels for delivery orders.
 * HIGH orders must be processed before MEDIUM and LOW orders.
 */
public enum OrderPriority {
    HIGH,
    MEDIUM,
    LOW;

    public static OrderPriority fromString(String value) {
        if (value == null) {
            return null;
        }
        return OrderPriority.valueOf(value.trim().toUpperCase());
    }
}
