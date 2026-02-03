package com.ironman.model;

public enum OrderStatus {
    PENDING,
    PICKUP_ASSIGNED,
    PICKED_UP,
    PROCESSING,
    QUALITY_CHECK,
    READY_FOR_DELIVERY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    REFUNDED
}