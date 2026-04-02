package com.hotel.service;

import com.hotel.model.DeluxeRoomProfile;
import com.hotel.model.HotelRoomProfile;
import com.hotel.model.RoomType;
import com.hotel.model.StandardRoomProfile;
import com.hotel.model.SuiteRoomProfile;

public final class RoomProfileFactory {

    private RoomProfileFactory() {
    }

    public static HotelRoomProfile create(RoomType roomType) {
        return switch (roomType) {
            case STANDARD -> new StandardRoomProfile();
            case DELUXE -> new DeluxeRoomProfile();
            case SUITE -> new SuiteRoomProfile();
        };
    }
}
