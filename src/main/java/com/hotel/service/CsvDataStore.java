package com.hotel.service;

import com.hotel.model.Bill;
import com.hotel.model.Booking;
import com.hotel.model.BookingStatus;
import com.hotel.model.Room;
import com.hotel.model.RoomStatus;
import com.hotel.model.RoomType;
import com.hotel.model.ServiceRequestRecord;
import com.hotel.model.ServiceRequestStatus;
import com.hotel.model.ServiceType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CsvDataStore {
    private static final Path DATA_DIR = Path.of("data");
    private static final Path ROOMS_FILE = DATA_DIR.resolve("rooms.csv");
    private static final Path BOOKINGS_FILE = DATA_DIR.resolve("bookings.csv");
    private static final Path BILLS_FILE = DATA_DIR.resolve("bills.csv");
    private static final Path SERVICE_REQUESTS_FILE = DATA_DIR.resolve("service_requests.csv");

    public CsvDataStore() {
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(DATA_DIR);
            createIfMissing(ROOMS_FILE, List.of(
                    header("roomNumber", "type", "pricePerNight", "status"),
                    line("101", "STANDARD", "2200.0", "AVAILABLE"),
                    line("102", "STANDARD", "2300.0", "AVAILABLE"),
                    line("201", "DELUXE", "3400.0", "AVAILABLE"),
                    line("202", "DELUXE", "3600.0", "MAINTENANCE"),
                    line("301", "SUITE", "5200.0", "AVAILABLE")
            ));
            createIfMissing(BOOKINGS_FILE, List.of(
                    header("bookingId", "guestName", "phone", "email", "idProof", "roomNumber",
                            "checkIn", "checkOut", "status", "roomCharge", "serviceCharge", "notes")
            ));
            createIfMissing(BILLS_FILE, List.of(
                    header("billId", "bookingId", "guestName", "roomNumber", "roomCharge",
                            "serviceCharge", "tax", "totalAmount", "paid", "paymentMode")
            ));
            createIfMissing(SERVICE_REQUESTS_FILE, List.of(
                    header("requestId", "bookingId", "roomNumber", "guestName", "serviceType",
                            "status", "currentStage", "createdAt")
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize data storage", exception);
        }
    }

    private void createIfMissing(Path path, List<String> lines) throws IOException {
        if (!Files.exists(path)) {
            Files.write(path, lines, StandardCharsets.UTF_8);
        }
    }

    public List<Room> loadRooms() {
        return readLines(ROOMS_FILE).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(this::toRoom)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void saveRooms(List<Room> rooms) {
        List<String> lines = new ArrayList<>();
        lines.add(header("roomNumber", "type", "pricePerNight", "status"));
        for (Room room : rooms) {
            lines.add(line(room.getRoomNumber(), room.getType().name(),
                    String.valueOf(room.getPricePerNight()), room.getStatus().name()));
        }
        writeLines(ROOMS_FILE, lines);
    }

    public List<Booking> loadBookings() {
        return readLines(BOOKINGS_FILE).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(this::toBooking)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void saveBookings(List<Booking> bookings) {
        List<String> lines = new ArrayList<>();
        lines.add(header("bookingId", "guestName", "phone", "email", "idProof", "roomNumber",
                "checkIn", "checkOut", "status", "roomCharge", "serviceCharge", "notes"));
        for (Booking booking : bookings) {
            lines.add(line(booking.getBookingId(), booking.getGuestName(), booking.getPhone(), booking.getEmail(),
                    booking.getIdProof(), booking.getRoomNumber(), booking.getCheckIn().toString(),
                    booking.getCheckOut().toString(), booking.getStatus().name(),
                    String.valueOf(booking.getRoomCharge()), String.valueOf(booking.getServiceCharge()),
                    booking.getNotes()));
        }
        writeLines(BOOKINGS_FILE, lines);
    }

    public List<Bill> loadBills() {
        return readLines(BILLS_FILE).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(this::toBill)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void saveBills(List<Bill> bills) {
        List<String> lines = new ArrayList<>();
        lines.add(header("billId", "bookingId", "guestName", "roomNumber", "roomCharge",
                "serviceCharge", "tax", "totalAmount", "paid", "paymentMode"));
        for (Bill bill : bills) {
            lines.add(line(bill.getBillId(), bill.getBookingId(), bill.getGuestName(), bill.getRoomNumber(),
                    String.valueOf(bill.getRoomCharge()), String.valueOf(bill.getServiceCharge()),
                    String.valueOf(bill.getTax()), String.valueOf(bill.getTotalAmount()),
                    String.valueOf(bill.isPaid()), bill.getPaymentMode()));
        }
        writeLines(BILLS_FILE, lines);
    }

    public List<ServiceRequestRecord> loadServiceRequests() {
        return readLines(SERVICE_REQUESTS_FILE).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(this::toServiceRequest)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void saveServiceRequests(List<ServiceRequestRecord> serviceRequests) {
        List<String> lines = new ArrayList<>();
        lines.add(header("requestId", "bookingId", "roomNumber", "guestName", "serviceType",
                "status", "currentStage", "createdAt"));
        for (ServiceRequestRecord request : serviceRequests) {
            lines.add(line(request.getRequestId(), request.getBookingId(), request.getRoomNumber(),
                    request.getGuestName(), request.getServiceType().name(), request.getStatus().name(),
                    request.getCurrentStage(), request.getCreatedAt().toString()));
        }
        writeLines(SERVICE_REQUESTS_FILE, lines);
    }

    private Room toRoom(String row) {
        List<String> values = parse(row);
        return new Room(values.get(0), RoomType.valueOf(values.get(1)),
                Double.parseDouble(values.get(2)), RoomStatus.valueOf(values.get(3)));
    }

    private Booking toBooking(String row) {
        List<String> values = parse(row);
        return new Booking(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4),
                values.get(5), LocalDate.parse(values.get(6)), LocalDate.parse(values.get(7)),
                BookingStatus.valueOf(values.get(8)), Double.parseDouble(values.get(9)),
                Double.parseDouble(values.get(10)), values.get(11));
    }

    private Bill toBill(String row) {
        List<String> values = parse(row);
        return new Bill(values.get(0), values.get(1), values.get(2), values.get(3),
                Double.parseDouble(values.get(4)), Double.parseDouble(values.get(5)),
                Double.parseDouble(values.get(6)), Double.parseDouble(values.get(7)),
                Boolean.parseBoolean(values.get(8)), values.get(9));
    }

    private ServiceRequestRecord toServiceRequest(String row) {
        List<String> values = parse(row);
        if (values.size() >= 8) {
            return new ServiceRequestRecord(
                    values.get(0),
                    values.get(1),
                    values.get(2),
                    values.get(3),
                    ServiceType.valueOf(values.get(4)),
                    ServiceRequestStatus.valueOf(values.get(5)),
                    values.get(6),
                    LocalDateTime.parse(values.get(7))
            );
        }
        return new ServiceRequestRecord(
                values.get(0),
                "",
                values.get(1),
                values.get(2),
                ServiceType.valueOf(values.get(3)),
                ServiceRequestStatus.valueOf(values.get(4)),
                values.get(5),
                LocalDateTime.parse(values.get(6))
        );
    }

    private List<String> readLines(Path path) {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + path, exception);
        }
    }

    private void writeLines(Path path, List<String> lines) {
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write " + path, exception);
        }
    }

    private String header(String... values) {
        return line(values);
    }

    private String line(String... values) {
        return List.of(values).stream()
                .map(this::escape)
                .collect(Collectors.joining(","));
    }

    private String escape(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private List<String> parse(String row) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < row.length(); index++) {
            char character = row.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < row.length() && row.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString());
        return values;
    }
}
