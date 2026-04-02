package com.hotel.service;

import com.hotel.model.Bill;
import com.hotel.model.Booking;
import com.hotel.model.BookingSearchFilter;
import com.hotel.model.BookingStatus;
import com.hotel.model.HotelRoomProfile;
import com.hotel.model.HotelSnapshot;
import com.hotel.model.Room;
import com.hotel.model.RoomStatus;
import com.hotel.model.RoomType;
import com.hotel.model.ServiceRequestRecord;
import com.hotel.model.ServiceRequestStatus;
import com.hotel.model.ServiceType;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class HotelService {
    private final BillingCalculator billingCalculator = new BillingCalculator();
    private final AuditLogService auditLogService = new AuditLogService();
    private final BackupService backupService = new BackupService();
    private final ReportService reportService = new ReportService();
    private final RoomServiceManager roomServiceManager = new RoomServiceManager();
    private final CsvDataStore dataStore = new CsvDataStore();
    private final List<Room> rooms = new ArrayList<>(dataStore.loadRooms());
    private final List<Booking> bookings = new ArrayList<>(dataStore.loadBookings());
    private final List<Bill> bills = new ArrayList<>(dataStore.loadBills());
    private final List<ServiceRequestRecord> serviceRequests = new ArrayList<>(dataStore.loadServiceRequests());

    public List<Room> getRooms() {
        refreshBookingStatuses();
        rooms.sort(Comparator.comparing(Room::getRoomNumber));
        return rooms;
    }

    public List<Room> getAvailableRooms() {
        refreshBookingStatuses();
        return rooms.stream()
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .toList();
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        refreshBookingStatuses();
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return List.of();
        }
        return rooms.stream()
                .filter(room -> room.getStatus() != RoomStatus.MAINTENANCE)
                .filter(room -> isRoomAvailableForDates(room.getRoomNumber(), checkIn, checkOut))
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .toList();
    }

    public List<Booking> getBookings() {
        refreshBookingStatuses();
        bookings.sort(Comparator.comparing(Booking::getCheckIn).reversed());
        return bookings;
    }

    public List<Booking> getActiveBookings() {
        refreshBookingStatuses();
        return bookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.CHECKED_OUT)
                .sorted(Comparator.comparing(Booking::getCheckIn).reversed())
                .toList();
    }

    public List<Booking> getCheckedInBookings() {
        refreshBookingStatuses();
        return bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CHECKED_IN)
                .sorted(Comparator.comparing(Booking::getCheckIn).reversed())
                .toList();
    }

    public List<Bill> getBills() {
        refreshBookingStatuses();
        bills.sort(Comparator.comparing(Bill::getBillId).reversed());
        return bills;
    }

    public List<Bill> searchBills(String billId, String bookingId, String phoneNumber, String paymentMode, String paidStatus) {
        refreshBookingStatuses();
        return GenericFilter.filter(getBills(), bill ->
                matchesText(bill.getBillId(), billId)
                        && matchesText(bill.getBookingId(), bookingId)
                        && matchesText(getPhoneForBill(bill), phoneNumber)
                        && matchesText(bill.getPaymentMode(), paymentMode)
                        && matchesBillPaid(bill, paidStatus)
        );
    }

    public String getPhoneForBill(Bill bill) {
        if (bill == null) {
            return "";
        }
        return bookings.stream()
                .filter(booking -> booking.getBookingId().equalsIgnoreCase(bill.getBookingId()))
                .map(Booking::getPhone)
                .findFirst()
                .orElse("");
    }

    public synchronized Room addRoom(String roomNumber, RoomType type, double pricePerNight, RoomStatus status) {
        if (type == null) {
            throw new IllegalArgumentException("Room type is required.");
        }
        boolean exists = rooms.stream().anyMatch(room -> room.getRoomNumber().equalsIgnoreCase(roomNumber));
        if (exists) {
            throw new IllegalArgumentException("Room number already exists.");
        }
        Room room = new Room(roomNumber, type, pricePerNight, status);
        rooms.add(room);
        dataStore.saveRooms(rooms);
        auditLogService.log("ROOM_ADDED", roomNumber + " added as " + type + " with status " + status);
        return room;
    }

    public synchronized void updateRoomStatus(Room room, RoomStatus status) {
        room.setStatus(status);
        dataStore.saveRooms(rooms);
        auditLogService.log("ROOM_STATUS", room.getRoomNumber() + " changed to " + status);
    }

    public void putRoomUnderMaintenance(Room room, Consumer<String> logConsumer, Runnable onStatusChanged) {
        if (room == null) {
            throw new IllegalArgumentException("Please select a room first.");
        }
        synchronized (this) {
            room.setStatus(RoomStatus.MAINTENANCE);
            dataStore.saveRooms(rooms);
            auditLogService.log("ROOM_STATUS", room.getRoomNumber() + " changed to MAINTENANCE");
        }
        if (onStatusChanged != null) {
            onStatusChanged.run();
        }

        ServiceRequestRecord request = addServiceRequest("", room.getRoomNumber(), "Hotel Operations", ServiceType.MAINTENANCE);
        roomServiceManager.submitRequest(request, updated -> {
            saveServiceRequests();
            auditLogService.log("SERVICE", serviceMessage(updated));
            if (logConsumer != null) {
                logConsumer.accept(serviceMessage(updated));
            }
            if (onStatusChanged != null) {
                onStatusChanged.run();
            }
        }, () -> {
            synchronized (this) {
                room.setStatus(RoomStatus.AVAILABLE);
                dataStore.saveRooms(rooms);
                saveServiceRequests();
                auditLogService.log("ROOM_STATUS", room.getRoomNumber() + " auto-restored to AVAILABLE after maintenance");
            }
            if (logConsumer != null) {
                logConsumer.accept("Room " + room.getRoomNumber() + " is now AVAILABLE after maintenance.");
            }
            if (onStatusChanged != null) {
                onStatusChanged.run();
            }
        });
    }

    public void requestRoomService(String roomNumber, String guestName, ServiceType serviceType, Consumer<String> logConsumer, Runnable onStatusChanged) {
        requestRoomService("", roomNumber, guestName, serviceType, logConsumer, onStatusChanged);
    }

    public void requestRoomService(String bookingId, String roomNumber, String guestName, ServiceType serviceType,
                                   Consumer<String> logConsumer, Runnable onStatusChanged) {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Please select a valid room."));
        ServiceRequestRecord request = addServiceRequest(bookingId, room.getRoomNumber(), guestName, serviceType);
        roomServiceManager.submitRequest(request, updated -> {
            saveServiceRequests();
            auditLogService.log("SERVICE", serviceMessage(updated));
            if (logConsumer != null) {
                logConsumer.accept(serviceMessage(updated));
            }
            if (onStatusChanged != null) {
                onStatusChanged.run();
            }
        }, null);
    }

    public void requestRoomServiceForBooking(String bookingId, ServiceType serviceType, Consumer<String> logConsumer, Runnable onStatusChanged) {
        refreshBookingStatuses();
        Booking booking = bookings.stream()
                .filter(item -> item.getBookingId().equalsIgnoreCase(bookingId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Please select a valid booking."));

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalArgumentException("Service requests are allowed only for checked-in bookings.");
        }

        requestRoomService(booking.getBookingId(), booking.getRoomNumber(), booking.getGuestName(), serviceType, logConsumer, onStatusChanged);
    }

    public List<ServiceRequestRecord> getServiceRequests() {
        serviceRequests.sort(Comparator.comparing(ServiceRequestRecord::getCreatedAt).reversed());
        return new ArrayList<>(serviceRequests);
    }

    public List<ServiceRequestRecord> searchServiceRequests(String bookingId, String requestId, String roomNumber,
                                                            ServiceType serviceType, ServiceRequestStatus status) {
        return GenericFilter.filter(getServiceRequests(), request ->
                matchesText(request.getBookingId(), bookingId)
                        && matchesText(request.getRequestId(), requestId)
                        && matchesText(request.getRoomNumber(), roomNumber)
                        && (serviceType == null || request.getServiceType() == serviceType)
                        && (status == null || request.getStatus() == status)
        );
    }

    public synchronized Booking createBooking(String guestName, String phone, String email, String idProof,
                                              String roomNumber, LocalDate checkIn, LocalDate checkOut,
                                              double serviceCharge, String notes) {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Please select a valid room."));
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after the check-in date.");
        }
        if (!isRoomAvailableForDates(roomNumber, checkIn, checkOut)) {
            throw new IllegalArgumentException("Selected room is not available for the chosen dates.");
        }

        Integer nights = (int) Math.max(1, ChronoUnit.DAYS.between(checkIn, checkOut));
        BillingCalculator.BillingBreakdown breakdown = billingCalculator.calculate(
                room.getType(), nights, serviceCharge
        );

        Booking booking = new Booking(
                "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT),
                guestName, phone, email, idProof, roomNumber, checkIn, checkOut,
                BookingStatus.RESERVED, breakdown.roomCharge(), breakdown.serviceCharge(), notes
        );

        bookings.add(booking);
        bills.add(generateBill(booking));
        refreshBookingStatuses();
        saveAll();
        auditLogService.log("BOOKING_CREATED", booking.getBookingId() + " for room " + roomNumber + " guest " + guestName);
        return booking;
    }

    public synchronized void checkOutBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Please select a booking to check out.");
        }
        refreshBookingStatuses();
        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalArgumentException("This guest has already checked out.");
        }
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalArgumentException("This guest has not checked in yet.");
        }
        booking.setStatus(BookingStatus.CHECKED_OUT);
        findRoom(booking.getRoomNumber()).ifPresent(room -> room.setStatus(RoomStatus.AVAILABLE));
        saveAll();
        auditLogService.log("CHECK_OUT", booking.getBookingId() + " checked out from room " + booking.getRoomNumber());
    }

    public synchronized void markBillPaid(Bill bill, String paymentMode) {
        if (bill == null) {
            throw new IllegalArgumentException("Please select a bill to mark as paid.");
        }
        bill.setPaid(true);
        bill.setPaymentMode(paymentMode);
        saveAll();
        auditLogService.log("BILL_PAID", bill.getBillId() + " paid by " + paymentMode);
    }

    public int totalRooms() {
        return rooms.size();
    }

    public long availableRoomsCount() {
        return rooms.stream().filter(room -> room.getStatus() == RoomStatus.AVAILABLE).count();
    }

    public long activeBookingsCount() {
        refreshBookingStatuses();
        return bookings.stream().filter(booking -> booking.getStatus() == BookingStatus.CHECKED_IN).count();
    }

    public double collectedRevenue() {
        return bills.stream()
                .filter(Bill::isPaid)
                .mapToDouble(Bill::getTotalAmount)
                .sum();
    }

    public double pendingRevenue() {
        return bills.stream()
                .filter(bill -> !bill.isPaid())
                .mapToDouble(Bill::getTotalAmount)
                .sum();
    }

    public BillingCalculator.BillingBreakdown previewBill(RoomType roomType, Integer daysStayed, Double serviceCharge) {
        return billingCalculator.calculate(roomType, daysStayed, serviceCharge);
    }

    public HotelRoomProfile getRoomProfile(RoomType roomType) {
        return RoomProfileFactory.create(roomType);
    }

    public List<Booking> searchBookings(String query) {
        refreshBookingStatuses();
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return getBookings();
        }
        return GenericFilter.filter(getBookings(), booking ->
                booking.getBookingId().toLowerCase(Locale.ROOT).contains(normalized)
                        || booking.getGuestName().toLowerCase(Locale.ROOT).contains(normalized)
                        || booking.getRoomNumber().toLowerCase(Locale.ROOT).contains(normalized)
                        || booking.getPhone().toLowerCase(Locale.ROOT).contains(normalized)
                        || booking.getCheckIn().toString().toLowerCase(Locale.ROOT).contains(normalized)
                        || booking.getCheckOut().toString().toLowerCase(Locale.ROOT).contains(normalized)
        );
    }

    public List<Booking> searchBookings(BookingSearchFilter filter) {
        refreshBookingStatuses();
        if (filter == null) {
            return getBookings();
        }

        return GenericFilter.filter(getBookings(), booking ->
                matchesText(booking.getBookingId(), filter.bookingId())
                        && matchesText(booking.getGuestName(), filter.guestName())
                        && matchesText(booking.getPhone(), filter.phone())
                        && matchesText(booking.getRoomNumber(), filter.roomNumber())
                        && matchesDate(booking.getCheckIn(), filter.startDate())
                        && matchesDate(booking.getCheckOut(), filter.endDate())
                        && matchesStatus(booking, filter.status())
        );
    }

    public String getRoomAmenitiesSummary(RoomType roomType) {
        HotelRoomProfile profile = getRoomProfile(roomType);
        return "Room Type: " + roomType + System.lineSeparator()
                + "Base Tariff: Rs. " + String.format("%.2f", roomType.getBaseTariff()) + System.lineSeparator()
                + "Amenities:" + System.lineSeparator()
                + "- " + String.join(System.lineSeparator() + "- ", profile.getAmenities());
    }

    public Path exportHotelReport() {
        Path path = reportService.exportSummary(getRooms(), getBookings(), getBills());
        auditLogService.log("REPORT_EXPORTED", path.toString());
        return path;
    }

    public Path createSerializedBackup() {
        Path path = backupService.createBackup(new HotelSnapshot(getRooms(), getBookings(), getBills()));
        auditLogService.log("BACKUP_CREATED", path.toString());
        return path;
    }

    public List<String> getRecentAuditEntries() {
        return auditLogService.readRecentEntries(12);
    }

    private synchronized void refreshBookingStatuses() {
        boolean bookingChanged = false;
        LocalDate today = LocalDate.now();

        for (Room room : rooms) {
            if (room.getStatus() != RoomStatus.MAINTENANCE) {
                room.setStatus(RoomStatus.AVAILABLE);
            }
        }

        for (Booking booking : bookings) {
            BookingStatus computedStatus;
            if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
                computedStatus = BookingStatus.CHECKED_OUT;
            } else if (today.isBefore(booking.getCheckIn())) {
                computedStatus = BookingStatus.RESERVED;
            } else if (today.isAfter(booking.getCheckOut())) {
                computedStatus = BookingStatus.CHECKED_OUT;
            } else {
                computedStatus = BookingStatus.CHECKED_IN;
            }

            if (booking.getStatus() != computedStatus) {
                booking.setStatus(computedStatus);
                bookingChanged = true;
            }

            if (computedStatus == BookingStatus.CHECKED_IN) {
                findRoom(booking.getRoomNumber()).ifPresent(room -> {
                    if (room.getStatus() != RoomStatus.MAINTENANCE) {
                        room.setStatus(RoomStatus.OCCUPIED);
                    }
                });
            }
        }

        if (bookingChanged) {
            saveAll();
        } else {
            dataStore.saveRooms(rooms);
        }
    }

    private ServiceRequestRecord addServiceRequest(String bookingId, String roomNumber, String guestName, ServiceType serviceType) {
        ServiceRequestRecord request = new ServiceRequestRecord(
                "SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT),
                bookingId,
                roomNumber,
                guestName,
                serviceType
        );
        synchronized (serviceRequests) {
            serviceRequests.add(request);
        }
        saveServiceRequests();
        auditLogService.log("SERVICE", serviceMessage(request));
        return request;
    }

    private String serviceMessage(ServiceRequestRecord request) {
        String bookingReference = request.getBookingId().isBlank() ? "" : " | booking " + request.getBookingId();
        return "%s | %s%s | room %s | %s".formatted(
                request.getServiceType().getDisplayName(),
                request.getStatus(),
                bookingReference,
                request.getRoomNumber(),
                request.getCurrentStage()
        );
    }

    private boolean matchesText(String actual, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        return actual.toLowerCase(Locale.ROOT).contains(expected.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesDate(LocalDate actual, LocalDate expected) {
        return expected == null || actual.equals(expected);
    }

    private boolean matchesStatus(Booking booking, String expected) {
        if (expected == null || expected.isBlank() || "ALL".equalsIgnoreCase(expected)) {
            return true;
        }
        String actual = booking.getStatus() == BookingStatus.RESERVED ? "PENDING" : booking.getStatus().name();
        return actual.equalsIgnoreCase(expected.trim());
    }

    private boolean matchesBillPaid(Bill bill, String expected) {
        if (expected == null || expected.isBlank() || "ALL".equalsIgnoreCase(expected)) {
            return true;
        }
        if ("PAID".equalsIgnoreCase(expected)) {
            return bill.isPaid();
        }
        if ("UNPAID".equalsIgnoreCase(expected)) {
            return !bill.isPaid();
        }
        return true;
    }

    private boolean isRoomAvailableForDates(String roomNumber, LocalDate checkIn, LocalDate checkOut) {
        return bookings.stream()
                .filter(booking -> booking.getRoomNumber().equalsIgnoreCase(roomNumber))
                .filter(booking -> booking.getStatus() != BookingStatus.CHECKED_OUT)
                .noneMatch(booking -> datesOverlap(checkIn, checkOut, booking.getCheckIn(), booking.getCheckOut()));
    }

    private boolean datesOverlap(LocalDate firstStart, LocalDate firstEnd, LocalDate secondStart, LocalDate secondEnd) {
        return firstStart.isBefore(secondEnd) && secondStart.isBefore(firstEnd);
    }

    private Bill generateBill(Booking booking) {
        Integer daysStayed = (int) Math.max(1, ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut()));
        BillingCalculator.BillingBreakdown breakdown = billingCalculator.calculate(
                findRoom(booking.getRoomNumber()).map(Room::getType).orElse(RoomType.STANDARD),
                daysStayed,
                booking.getServiceCharge()
        );
        return new Bill(
                "BL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT),
                booking.getBookingId(), booking.getGuestName(), booking.getRoomNumber(),
                booking.getRoomCharge(), booking.getServiceCharge(), breakdown.taxAmount(), breakdown.totalAmount(),
                false, "Pending"
        );
    }

    private Optional<Room> findRoom(String roomNumber) {
        return rooms.stream()
                .filter(room -> room.getRoomNumber().equalsIgnoreCase(roomNumber))
                .findFirst();
    }

    private void saveAll() {
        dataStore.saveRooms(rooms);
        dataStore.saveBookings(bookings);
        dataStore.saveBills(bills);
        dataStore.saveServiceRequests(serviceRequests);
    }

    private void saveServiceRequests() {
        synchronized (serviceRequests) {
            dataStore.saveServiceRequests(serviceRequests);
        }
    }

    public void shutdown() {
        roomServiceManager.shutdown();
    }
}
