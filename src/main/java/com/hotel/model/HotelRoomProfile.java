package com.hotel.model;

import java.util.ArrayList;
import java.util.List;

public abstract class HotelRoomProfile implements AmenityProvider {
    private final RoomType roomType;

    protected HotelRoomProfile(RoomType roomType) {
        this.roomType = roomType;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public String getDisplayName() {
        return roomType.name();
    }

    @Override
    public List<String> getAmenities() {
        List<String> amenities = new ArrayList<>();
        if (provideWifi()) {
            amenities.add("WiFi");
        }
        if (provideBreakfast()) {
            amenities.add("Breakfast");
        }
        addTypeSpecificAmenities(amenities);
        return amenities;
    }

    protected abstract void addTypeSpecificAmenities(List<String> amenities);
}
