package com.hotel.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

public class Booking implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String bookingId;
    private final String guestName;
    private final String phone;
    private final String email;
    private final String idProof;
    private final String roomNumber;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private BookingStatus status;
    private double roomCharge;
    private double serviceCharge;
    private final String notes;

    public Booking(String bookingId, String guestName, String phone, String email, String idProof,
                   String roomNumber, LocalDate checkIn, LocalDate checkOut, BookingStatus status,
                   double roomCharge, double serviceCharge, String notes) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.phone = phone;
        this.email = email;
        this.idProof = idProof;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.roomCharge = roomCharge;
        this.serviceCharge = serviceCharge;
        this.notes = notes;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getIdProof() {
        return idProof;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public double getRoomCharge() {
        return roomCharge;
    }

    public void setRoomCharge(double roomCharge) {
        this.roomCharge = roomCharge;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public String getNotes() {
        return notes;
    }

    public double getTotalCharge() {
        return roomCharge + serviceCharge;
    }
}
