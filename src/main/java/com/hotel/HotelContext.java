package com.hotel;

import com.hotel.service.HotelService;

public final class HotelContext {
    private static final HotelService HOTEL_SERVICE = new HotelService();

    private HotelContext() {
    }

    public static HotelService hotelService() {
        return HOTEL_SERVICE;
    }

    public static void shutdown() {
        HOTEL_SERVICE.shutdown();
    }
}
