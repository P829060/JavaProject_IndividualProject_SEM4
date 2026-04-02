package com.hotel.model;

public enum RoomType {
    STANDARD(2200.0),
    DELUXE(3400.0),
    SUITE(5200.0);

    private final double baseTariff;

    RoomType(double baseTariff) {
        this.baseTariff = baseTariff;
    }

    public double getBaseTariff() {
        return baseTariff;
    }

    public double calculateRoomCost(int numberOfDays) {
        return baseTariff * Math.max(1, numberOfDays);
    }
}
