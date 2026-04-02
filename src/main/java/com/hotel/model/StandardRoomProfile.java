package com.hotel.model;

import java.util.List;

public class StandardRoomProfile extends HotelRoomProfile {

    public StandardRoomProfile() {
        super(RoomType.STANDARD);
    }

    @Override
    public boolean provideWifi() {
        return true;
    }

    @Override
    public boolean provideBreakfast() {
        return false;
    }

    @Override
    protected void addTypeSpecificAmenities(List<String> amenities) {
        amenities.add("Queen Bed");
        amenities.add("Daily Housekeeping");
    }
}
