package com.hotel.model;

import java.time.LocalDate;

public record BookingSearchFilter(
        String bookingId,
        String guestName,
        String phone,
        String roomNumber,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {
}
