package com.hotel.model;

public class RoomServiceRequest {
    private final String roomNumber;
    private final String serviceName;
    private final int durationMillis;

    public RoomServiceRequest(String roomNumber, String serviceName, int durationMillis) {
        this.roomNumber = roomNumber;
        this.serviceName = serviceName;
        this.durationMillis = durationMillis;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getDurationMillis() {
        return durationMillis;
    }
}
