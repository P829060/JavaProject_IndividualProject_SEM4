package com.hotel.model;

import java.util.List;

public class SuiteRoomProfile extends HotelRoomProfile {

    public SuiteRoomProfile() {
        super(RoomType.SUITE);
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
        amenities.add("Living Area");
        amenities.add("Bathtub");
        amenities.add("Priority Room Service");
    }
}
