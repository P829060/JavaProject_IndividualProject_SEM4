package com.hotel.service;

import com.hotel.model.RoomType;

public class BillingCalculator {
    private static final Double TAX_RATE = 0.18;

    public BillingBreakdown calculate(RoomType roomType, Integer numberOfDays, Double serviceCharge) {
        if (roomType == null) {
            throw new IllegalArgumentException("Room type is required for billing.");
        }
        Integer safeDays = numberOfDays == null ? 1 : Math.max(1, numberOfDays);
        Double safeServiceCharge = serviceCharge == null ? 0.0 : Math.max(0.0, serviceCharge);

        Double tariffWrapper = roomType.getBaseTariff();
        Double roomCharge = roomType.calculateRoomCost(safeDays);
        Double subTotal = roomCharge + safeServiceCharge;
        Double taxAmount = subTotal * TAX_RATE;
        Double totalAmount = subTotal + taxAmount;

        return new BillingBreakdown(tariffWrapper, safeDays, safeServiceCharge, roomCharge, taxAmount, totalAmount);
    }

    public record BillingBreakdown(
            Double roomTariff,
            Integer daysStayed,
            Double serviceCharge,
            Double roomCharge,
            Double taxAmount,
            Double totalAmount
    ) {
        public String asDisplayText(RoomType roomType) {
            return """
                    Room Type: %s
                    Wrapper Tariff Value: %.2f
                    Days Stayed (Integer wrapper): %d
                    Extra Service Charge (Double wrapper): %.2f
                    Room Charge: %.2f
                    Tax: %.2f
                    Final Hotel Bill: %.2f
                    """.formatted(
                    roomType.name(),
                    roomTariff,
                    daysStayed,
                    serviceCharge,
                    roomCharge,
                    taxAmount,
                    totalAmount
            );
        }
    }
}
