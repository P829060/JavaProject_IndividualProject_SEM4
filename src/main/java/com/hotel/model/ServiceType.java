package com.hotel.model;

import java.util.List;

public enum ServiceType {
    MAINTENANCE("Maintenance", List.of("Queued for maintenance", "Maintenance in process", "Completed"), 4200),
    FOOD_DELIVERY("Food Delivery", List.of("Request received", "Food is being prepared", "Sent to room", "Received by customer", "Completed"), 2300),
    LAUNDRY_PICKUP("Laundry Pickup", List.of("Request received", "Sent to laundry", "Laundry in process", "Sent back to room", "Completed"), 2500),
    ROOM_CLEANING("Room Cleaning", List.of("Request received", "Cleaning in process", "Final room check", "Completed"), 2400);

    private final String displayName;
    private final List<String> stages;
    private final int stageDurationMillis;

    ServiceType(String displayName, List<String> stages, int stageDurationMillis) {
        this.displayName = displayName;
        this.stages = stages;
        this.stageDurationMillis = stageDurationMillis;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getStages() {
        return stages;
    }

    public int getStageDurationMillis() {
        return stageDurationMillis;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
