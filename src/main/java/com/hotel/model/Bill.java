package com.hotel.model;

import java.io.Serial;
import java.io.Serializable;

public class Bill implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String billId;
    private final String bookingId;
    private final String guestName;
    private final String roomNumber;
    private final double roomCharge;
    private final double serviceCharge;
    private final double tax;
    private final double totalAmount;
    private boolean paid;
    private String paymentMode;

    public Bill(String billId, String bookingId, String guestName, String roomNumber,
                double roomCharge, double serviceCharge, double tax, double totalAmount,
                boolean paid, String paymentMode) {
        this.billId = billId;
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.roomCharge = roomCharge;
        this.serviceCharge = serviceCharge;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.paid = paid;
        this.paymentMode = paymentMode;
    }

    public String getBillId() {
        return billId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public double getRoomCharge() {
        return roomCharge;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public double getTax() {
        return tax;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }
}
