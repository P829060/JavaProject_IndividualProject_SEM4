package com.hotel.model;

import java.io.Serial;
import java.io.Serializable;

public class Room implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomNumber;
    private RoomType type;
    private double pricePerNight;
    private RoomStatus status;

    public Room(String roomNumber, RoomType type, double pricePerNight, RoomStatus status) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}
