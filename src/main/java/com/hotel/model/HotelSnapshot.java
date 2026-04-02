package com.hotel.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HotelSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final LocalDateTime createdAt;
    private final List<Room> rooms;
    private final List<Booking> bookings;
    private final List<Bill> bills;

    public HotelSnapshot(List<Room> rooms, List<Booking> bookings, List<Bill> bills) {
        this.createdAt = LocalDateTime.now();
        this.rooms = new ArrayList<>(rooms);
        this.bookings = new ArrayList<>(bookings);
        this.bills = new ArrayList<>(bills);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Room> getRooms() {
        return new ArrayList<>(rooms);
    }

    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    public List<Bill> getBills() {
        return new ArrayList<>(bills);
    }
}
