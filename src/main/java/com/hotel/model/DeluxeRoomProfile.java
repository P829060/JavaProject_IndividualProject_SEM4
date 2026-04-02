package com.hotel.model;

import java.util.List;

public class DeluxeRoomProfile extends HotelRoomProfile {

    public DeluxeRoomProfile() {
        super(RoomType.DELUXE);
    }

    @Override
    public boolean provideWifi() {
        return true;
    }

    @Override
    public boolean provideBreakfast() {
        return true;
    }

    @Override
    protected void addTypeSpecificAmenities(List<String> amenities) {
        amenities.add("King Bed");
        amenities.add("Mini Bar");
        amenities.add("Work Desk");
    }
}
