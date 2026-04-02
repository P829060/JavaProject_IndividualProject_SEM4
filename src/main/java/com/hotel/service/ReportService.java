package com.hotel.service;

import com.hotel.model.Bill;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {
    private static final Path REPORT_DIR = Path.of("data", "reports");
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public Path exportSummary(List<Room> rooms, List<Booking> bookings, List<Bill> bills) {
        try {
            Files.createDirectories(REPORT_DIR);
            Path reportPath = REPORT_DIR.resolve("hotel-report-" + LocalDate.now().format(REPORT_DATE_FORMAT) + ".txt");
            try (BufferedWriter writer = Files.newBufferedWriter(reportPath, StandardCharsets.UTF_8)) {
                writer.write("HOTEL MANAGEMENT REPORT");
                writer.newLine();
                writer.write("Generated: " + LocalDateTime.now());
                writer.newLine();
                writer.newLine();
                writer.write("ROOMS");
                writer.newLine();
                for (Room room : rooms) {
                    writer.write("%s | %s | %.2f | %s".formatted(
                            room.getRoomNumber(),
                            room.getType(),
                            room.getPricePerNight(),
                            room.getStatus()
                    ));
                    writer.newLine();
                }
                writer.newLine();
                writer.write("BOOKINGS");
                writer.newLine();
                for (Booking booking : bookings) {
                    writer.write("%s | %s | %s | %s to %s | %s".formatted(
                            booking.getBookingId(),
                            booking.getGuestName(),
                            booking.getRoomNumber(),
                            booking.getCheckIn(),
                            booking.getCheckOut(),
                            booking.getStatus()
                    ));
                    writer.newLine();
                }
                writer.newLine();
                writer.write("BILLS");
                writer.newLine();
                for (Bill bill : bills) {
                    writer.write("%s | %s | %.2f | paid=%s | %s".formatted(
                            bill.getBillId(),
                            bill.getGuestName(),
                            bill.getTotalAmount(),
                            bill.isPaid(),
                            bill.getPaymentMode()
                    ));
                    writer.newLine();
                }
            }
            return reportPath;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to export report", exception);
        }
    }
}
