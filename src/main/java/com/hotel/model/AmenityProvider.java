package com.hotel.model;

import java.util.List;

public interface AmenityProvider {
    boolean provideWifi();
    boolean provideBreakfast();
    List<String> getAmenities();
}
