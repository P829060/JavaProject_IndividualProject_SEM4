package com.hotel.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ServiceRequestRecord implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String requestId;
    private final String bookingId;
    private final String roomNumber;
    private final String guestName;
    private final ServiceType serviceType;
    private volatile ServiceRequestStatus status;
    private volatile String currentStage;
    private final LocalDateTime createdAt;

    public ServiceRequestRecord(String requestId, String bookingId, String roomNumber, String guestName, ServiceType serviceType) {
        this.requestId = requestId;
        this.bookingId = bookingId == null ? "" : bookingId;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.serviceType = serviceType;
        this.status = ServiceRequestStatus.PENDING;
        this.currentStage = serviceType.getStages().getFirst();
        this.createdAt = LocalDateTime.now();
    }

    public ServiceRequestRecord(String requestId, String bookingId, String roomNumber, String guestName,
                                ServiceType serviceType, ServiceRequestStatus status,
                                String currentStage, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.bookingId = bookingId == null ? "" : bookingId;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.serviceType = serviceType;
        this.status = status;
        this.currentStage = currentStage;
        this.createdAt = createdAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public ServiceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceRequestStatus status) {
        this.status = status;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
