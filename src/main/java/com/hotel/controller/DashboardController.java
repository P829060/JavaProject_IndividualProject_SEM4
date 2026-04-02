package com.hotel.controller;

import com.hotel.HotelContext;
import com.hotel.model.Bill;
import com.hotel.model.Booking;
import com.hotel.model.BookingSearchFilter;
import com.hotel.model.Room;
import com.hotel.model.RoomStatus;
import com.hotel.model.RoomType;
import com.hotel.model.ServiceRequestRecord;
import com.hotel.model.ServiceRequestStatus;
import com.hotel.model.ServiceType;
import com.hotel.service.BillingCalculator;
import com.hotel.service.HotelService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.hotel.util.ViewLoader;

public class DashboardController {
    private final HotelService hotelService = HotelContext.hotelService();

    @FXML private Label todayLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label activeBookingsLabel;
    @FXML private Label collectedRevenueLabel;
    @FXML private Label pendingRevenueLabel;

    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> roomNumberColumn;
    @FXML private TableColumn<Room, RoomType> roomTypeColumn;
    @FXML private TableColumn<Room, Number> roomPriceColumn;
    @FXML private TableColumn<Room, RoomStatus> roomStatusColumn;

    @FXML private TextField roomNumberField;
    @FXML private ComboBox<RoomType> roomTypeField;
    @FXML private TextField roomPriceField;

    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> guestNameColumn;
    @FXML private TableColumn<Booking, String> guestPhoneColumn;
    @FXML private TableColumn<Booking, String> bookingRoomColumn;
    @FXML private TableColumn<Booking, LocalDate> checkInColumn;
    @FXML private TableColumn<Booking, LocalDate> checkOutColumn;
    @FXML private TableColumn<Booking, String> bookingStatusColumn;
    @FXML private TableColumn<Booking, Number> bookingAmountColumn;

    @FXML private TextField guestNameField;
    @FXML private TextField guestPhoneField;
    @FXML private TextField guestEmailField;
    @FXML private TextField guestIdField;
    @FXML private ComboBox<String> bookingRoomField;
    @FXML private DatePicker checkInField;
    @FXML private DatePicker checkOutField;
    @FXML private TextField serviceChargeField;
    @FXML private TextArea bookingNotesField;

    @FXML private TableView<Bill> billTable;
    @FXML private TableColumn<Bill, String> billIdColumn;
    @FXML private TableColumn<Bill, String> billBookingIdColumn;
    @FXML private TableColumn<Bill, String> billGuestColumn;
    @FXML private TableColumn<Bill, String> billPhoneColumn;
    @FXML private TableColumn<Bill, String> billRoomColumn;
    @FXML private TableColumn<Bill, Number> billTotalColumn;
    @FXML private TableColumn<Bill, Boolean> billPaidColumn;
    @FXML private TableColumn<Bill, String> billPaymentModeColumn;

    @FXML private ComboBox<String> paymentModeField;
    @FXML private TextField billSearchIdField;
    @FXML private TextField billSearchBookingIdField;
    @FXML private TextField billSearchPhoneField;
    @FXML private ComboBox<String> billSearchPaidField;
    @FXML private ComboBox<String> billSearchPaymentModeField;
    @FXML private TableView<ServiceRequestRecord> serviceRequestTable;
    @FXML private TableColumn<ServiceRequestRecord, String> serviceRequestIdColumn;
    @FXML private TableColumn<ServiceRequestRecord, String> serviceBookingIdColumn;
    @FXML private TableColumn<ServiceRequestRecord, String> serviceRoomColumn;
    @FXML private TableColumn<ServiceRequestRecord, String> serviceGuestColumn;
    @FXML private TableColumn<ServiceRequestRecord, ServiceType> serviceTypeColumn;
    @FXML private TableColumn<ServiceRequestRecord, ServiceRequestStatus> serviceStatusColumn;
    @FXML private TableColumn<ServiceRequestRecord, String> serviceStageColumn;
    @FXML private ComboBox<Booking> serviceBookingField;
    @FXML private ComboBox<ServiceType> serviceTypeField;
    @FXML private Label pendingMaintenanceLabel;
    @FXML private Label pendingFoodLabel;
    @FXML private Label pendingLaundryLabel;
    @FXML private TextField serviceSearchBookingIdField;
    @FXML private TextField serviceSearchRequestIdField;
    @FXML private TextField serviceSearchRoomField;
    @FXML private ComboBox<ServiceType> serviceSearchTypeField;
    @FXML private ComboBox<String> serviceSearchStatusField;
    @FXML private TextField bookingSearchIdField;
    @FXML private TextField bookingSearchGuestField;
    @FXML private TextField bookingSearchPhoneField;
    @FXML private TextField bookingSearchRoomField;
    @FXML private DatePicker bookingSearchStartDateField;
    @FXML private DatePicker bookingSearchEndDateField;
    @FXML private ComboBox<String> bookingSearchStatusField;
    @FXML private ComboBox<RoomType> amenitiesRoomTypeField;
    @FXML private TextArea amenitiesOutputArea;
    @FXML private TextArea auditLogArea;

    @FXML
    private void initialize() {
        setupRoomTable();
        setupBookingTable();
        setupBillTable();
        setupServiceRequestTable();
        roomTypeField.setItems(FXCollections.observableArrayList(RoomType.values()));
        paymentModeField.setItems(FXCollections.observableArrayList("Cash", "Card", "UPI", "Net Banking"));
        billSearchPaidField.setItems(FXCollections.observableArrayList("ALL", "PAID", "UNPAID"));
        billSearchPaidField.setValue("ALL");
        billSearchPaymentModeField.setItems(FXCollections.observableArrayList("ALL", "Pending", "Cash", "Card", "UPI", "Net Banking"));
        billSearchPaymentModeField.setValue("ALL");
        serviceTypeField.setItems(FXCollections.observableArrayList(ServiceType.FOOD_DELIVERY, ServiceType.LAUNDRY_PICKUP, ServiceType.ROOM_CLEANING));
        serviceTypeField.setValue(ServiceType.FOOD_DELIVERY);
        serviceSearchTypeField.setItems(FXCollections.observableArrayList(ServiceType.values()));
        serviceSearchStatusField.setItems(FXCollections.observableArrayList("ALL", "PENDING", "IN_PROGRESS", "COMPLETED"));
        serviceSearchStatusField.setValue("ALL");
        amenitiesRoomTypeField.setItems(FXCollections.observableArrayList(RoomType.values()));
        amenitiesRoomTypeField.setValue(RoomType.STANDARD);
        bookingSearchStatusField.setItems(FXCollections.observableArrayList("ALL", "PENDING", "CHECKED_IN", "CHECKED_OUT"));
        bookingSearchStatusField.setValue("ALL");
        configureBookingDatePickers();
        todayLabel.setText(LocalDate.now().toString());
        serviceBookingField.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Booking booking) {
                if (booking == null) {
                    return "";
                }
                return booking.getBookingId() + " | " + booking.getGuestName() + " | Room " + booking.getRoomNumber();
            }

            @Override
            public Booking fromString(String string) {
                return null;
            }
        });
        refreshAll();
        handleShowAmenities();
        handleRefreshAuditLog();
    }

    private void setupRoomTable() {
        roomNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        roomTypeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getType()));
        roomPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPricePerNight()));
        roomStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));
    }

    private void setupBookingTable() {
        bookingIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookingId()));
        guestNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGuestName()));
        guestPhoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        bookingRoomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        checkInColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCheckIn()));
        checkOutColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCheckOut()));
        bookingStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(displayBookingStatus(data.getValue())));
        bookingAmountColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalCharge()));
    }

    private void setupBillTable() {
        billIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBillId()));
        billBookingIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookingId()));
        billGuestColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGuestName()));
        billPhoneColumn.setCellValueFactory(data -> new SimpleStringProperty(hotelService.getPhoneForBill(data.getValue())));
        billRoomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        billTotalColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalAmount()));
        billPaidColumn.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isPaid()));
        billPaymentModeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentMode()));
    }

    private void setupServiceRequestTable() {
        serviceRequestIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequestId()));
        serviceBookingIdColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBookingId().isBlank() ? "N/A" : data.getValue().getBookingId()
        ));
        serviceRoomColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        serviceGuestColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGuestName()));
        serviceTypeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getServiceType()));
        serviceStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));
        serviceStageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCurrentStage()));
    }

    @FXML
    private void handleAddRoom() {
        try {
            RoomType selectedType = required(roomTypeField.getValue(), "Room type");
            double priceToStore = Double.parseDouble(required(roomPriceField.getText(), "Room price"));
            if (priceToStore <= 0) {
                throw new IllegalArgumentException("Room price must be greater than 0.");
            }
            hotelService.addRoom(
                    required(roomNumberField.getText(), "Room number"),
                    selectedType,
                    priceToStore,
                    RoomStatus.AVAILABLE
            );
            clearRoomForm();
            refreshAll();
            showInfo("Room added successfully. New rooms are available by default.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleSetMaintenance() {
        try {
            Room room = roomTable.getSelectionModel().getSelectedItem();
            if (room == null) {
                throw new IllegalArgumentException("Please select a room first.");
            }
            if (room.getStatus() == RoomStatus.OCCUPIED) {
                throw new IllegalArgumentException("Occupied rooms cannot be forced to available or maintenance. Please check out the guest first.");
            }
            if (room.getStatus() == RoomStatus.MAINTENANCE) {
                throw new IllegalArgumentException("This room is already under maintenance.");
            }
            hotelService.putRoomUnderMaintenance(
                    room,
                    this::appendServiceActivity,
                    () -> Platform.runLater(this::refreshAll)
            );
            refreshAll();
            showInfo("Room moved to maintenance. It will return to available automatically.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleCreateBooking() {
        try {
            LocalDate checkIn = required(checkInField.getValue(), "Check-in date");
            LocalDate checkOut = required(checkOutField.getValue(), "Check-out date");
            long dayDifference = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (dayDifference <= 0) {
                throw new IllegalArgumentException("Check-out date must be after the check-in date.");
            }
            Room selectedRoom = hotelService.getRooms().stream()
                    .filter(room -> room.getRoomNumber().equalsIgnoreCase(required(bookingRoomField.getValue(), "Room")))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Please select an available room."));
            BillingCalculator.BillingBreakdown breakdown = hotelService.previewBill(
                    selectedRoom.getType(),
                    (int) dayDifference,
                    parseMoney(serviceChargeField.getText())
            );
            hotelService.createBooking(
                    required(guestNameField.getText(), "Guest name"),
                    required(guestPhoneField.getText(), "Phone number"),
                    required(guestEmailField.getText(), "Email address"),
                    required(guestIdField.getText(), "ID proof"),
                    required(bookingRoomField.getValue(), "Room"),
                    checkIn,
                    checkOut,
                    breakdown.serviceCharge(),
                    bookingNotesField.getText().trim()
            );
            clearBookingForm();
            refreshAll();
            showInfo("Booking created and bill generated.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleCheckOut() {
        try {
            Booking booking = bookingTable.getSelectionModel().getSelectedItem();
            if (booking == null) {
                throw new IllegalArgumentException("Please select a booking to check out.");
            }
            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION,
                    "Check out booking " + booking.getBookingId() + "?").showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                hotelService.checkOutBooking(booking);
                refreshAll();
                showInfo("Guest checked out and room made available.");
            }
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleMarkPaid() {
        try {
            Bill bill = billTable.getSelectionModel().getSelectedItem();
            hotelService.markBillPaid(bill, required(paymentModeField.getValue(), "Payment mode"));
            refreshAll();
            showInfo("Bill marked as paid.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleShowSelectedBill() {
        Bill bill = billTable.getSelectionModel().getSelectedItem();
        if (bill == null) {
            showError("Please select a bill first.");
            return;
        }
        String invoice = """
                Bill ID: %s
                Booking ID: %s
                Guest: %s
                Room: %s
                Room Charge: %.2f
                Service Charge: %.2f
                Tax: %.2f
                Total Amount: %.2f
                Paid: %s
                Payment Mode: %s
                """.formatted(
                bill.getBillId(), bill.getBookingId(), bill.getGuestName(), bill.getRoomNumber(),
                bill.getRoomCharge(), bill.getServiceCharge(), bill.getTax(), bill.getTotalAmount(),
                bill.isPaid() ? "Yes" : "No", bill.getPaymentMode()
        );
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Invoice Preview");
        alert.setContentText(invoice);
        alert.showAndWait();
    }

    @FXML
    private void handleRequestService() {
        try {
            Booking booking = required(serviceBookingField.getValue(), "Booking");
            if (booking == null) {
                throw new IllegalArgumentException("Please select a booking first.");
            }
            hotelService.requestRoomServiceForBooking(
                    booking.getBookingId(),
                    required(serviceTypeField.getValue(), "Service type"),
                    this::appendServiceActivity,
                    () -> Platform.runLater(this::refreshAll)
            );
            refreshAll();
            showInfo("Service request started in the background.");
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleSearchBills() {
        String paymentMode = billSearchPaymentModeField.getValue();
        if ("ALL".equalsIgnoreCase(paymentMode)) {
            paymentMode = "";
        }
        List<Bill> results = hotelService.searchBills(
                billSearchIdField.getText(),
                billSearchBookingIdField.getText(),
                billSearchPhoneField.getText(),
                paymentMode,
                billSearchPaidField.getValue()
        );
        billTable.setItems(FXCollections.observableArrayList(results));
        billTable.refresh();
    }

    @FXML
    private void handleClearBillSearch() {
        billSearchIdField.clear();
        billSearchBookingIdField.clear();
        billSearchPhoneField.clear();
        billSearchPaidField.setValue("ALL");
        billSearchPaymentModeField.setValue("ALL");
        handleSearchBills();
    }

    @FXML
    private void handleSearchServiceRequests() {
        ServiceType serviceType = serviceSearchTypeField.getValue();
        String statusText = serviceSearchStatusField.getValue();
        ServiceRequestStatus status = null;
        if (statusText != null && !"ALL".equalsIgnoreCase(statusText)) {
            status = ServiceRequestStatus.valueOf(statusText);
        }
        List<ServiceRequestRecord> results = hotelService.searchServiceRequests(
                serviceSearchBookingIdField.getText(),
                serviceSearchRequestIdField.getText(),
                serviceSearchRoomField.getText(),
                serviceType,
                status
        );
        serviceRequestTable.setItems(FXCollections.observableArrayList(results));
        serviceRequestTable.refresh();
    }

    @FXML
    private void handleClearServiceRequestSearch() {
        serviceSearchBookingIdField.clear();
        serviceSearchRequestIdField.clear();
        serviceSearchRoomField.clear();
        serviceSearchTypeField.getSelectionModel().clearSelection();
        serviceSearchStatusField.setValue("ALL");
        handleSearchServiceRequests();
    }

    @FXML
    private void handleSearchBookings() {
        BookingSearchFilter filter = new BookingSearchFilter(
                bookingSearchIdField.getText(),
                bookingSearchGuestField.getText(),
                bookingSearchPhoneField.getText(),
                bookingSearchRoomField.getText(),
                bookingSearchStartDateField.getValue(),
                bookingSearchEndDateField.getValue(),
                bookingSearchStatusField.getValue()
        );
        List<Booking> results = hotelService.searchBookings(filter);
        bookingTable.setItems(FXCollections.observableArrayList(results));
        bookingTable.refresh();
    }

    @FXML
    private void handleClearBookingSearch() {
        bookingSearchIdField.clear();
        bookingSearchGuestField.clear();
        bookingSearchPhoneField.clear();
        bookingSearchRoomField.clear();
        bookingSearchStartDateField.setValue(null);
        bookingSearchEndDateField.setValue(null);
        bookingSearchStatusField.setValue("ALL");
        handleSearchBookings();
    }

    @FXML
    private void handleShowAmenities() {
        RoomType selectedType = amenitiesRoomTypeField.getValue();
        if (selectedType == null) {
            amenitiesOutputArea.clear();
            return;
        }
        amenitiesOutputArea.setText(hotelService.getRoomAmenitiesSummary(selectedType));
    }

    @FXML
    private void handleExportReport() {
        try {
            showInfo("Report saved to: " + hotelService.exportHotelReport());
            handleRefreshAuditLog();
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleCreateBackup() {
        try {
            showInfo("Backup saved to: " + hotelService.createSerializedBackup());
            handleRefreshAuditLog();
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleRefreshAuditLog() {
        List<String> entries = hotelService.getRecentAuditEntries();
        auditLogArea.setText(entries.isEmpty() ? "No recent audit entries." : String.join(System.lineSeparator(), entries));
    }

    @FXML
    private void handleViewBookingDetails() {
        Booking booking = bookingTable.getSelectionModel().getSelectedItem();
        if (booking == null) {
            showError("Please select a booking first.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Booking Details");
        alert.setTitle("Booking Details");
        alert.setContentText(formatBookingDetail(booking));
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION,
                    "Do you want to log out and return to the login screen?").showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Stage stage = (Stage) todayLabel.getScene().getWindow();
                Scene scene = new Scene(ViewLoader.load("login-view.fxml"), 1000, 680);
                scene.getStylesheets().add(ViewLoader.resource("css/app.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Hotel Management Application");
            }
        } catch (Exception exception) {
            showError("Unable to log out right now.");
        }
    }

    private void refreshAll() {
        roomTable.setItems(FXCollections.observableArrayList(hotelService.getRooms()));
        serviceBookingField.setItems(FXCollections.observableArrayList(hotelService.getCheckedInBookings()));
        updateAvailableRoomChoices();
        roomTable.refresh();
        serviceRequestTable.refresh();
        totalRoomsLabel.setText(String.valueOf(hotelService.totalRooms()));
        availableRoomsLabel.setText(String.valueOf(hotelService.availableRoomsCount()));
        activeBookingsLabel.setText(String.valueOf(hotelService.activeBookingsCount()));
        collectedRevenueLabel.setText(String.format("Rs. %.2f", hotelService.collectedRevenue()));
        pendingRevenueLabel.setText(String.format("Rs. %.2f", hotelService.pendingRevenue()));
        pendingMaintenanceLabel.setText(String.valueOf(countOpenRequests(ServiceType.MAINTENANCE)));
        pendingFoodLabel.setText(String.valueOf(countOpenRequests(ServiceType.FOOD_DELIVERY)));
        pendingLaundryLabel.setText(String.valueOf(countOpenRequests(ServiceType.LAUNDRY_PICKUP)));
        handleSearchBills();
        handleSearchServiceRequests();
        handleSearchBookings();
        bookingTable.refresh();
        billTable.refresh();
        handleRefreshAuditLog();
    }

    private void appendServiceActivity(String message) {
        Platform.runLater(this::refreshAll);
    }

    private String formatBookingDetail(Booking booking) {
        return """
                Booking ID: %s
                Guest Name: %s
                Phone: %s
                Email: %s
                ID Proof: %s
                Room: %s
                Start Date: %s
                End Date: %s
                Status: %s
                Room Charge: %.2f
                Service Charge: %.2f
                Notes: %s
                """.formatted(
                booking.getBookingId(),
                booking.getGuestName(),
                booking.getPhone(),
                booking.getEmail(),
                booking.getIdProof(),
                booking.getRoomNumber(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                displayBookingStatus(booking),
                booking.getRoomCharge(),
                booking.getServiceCharge(),
                booking.getNotes().isBlank() ? "-" : booking.getNotes()
        );
    }

    private String displayBookingStatus(Booking booking) {
        return booking.getStatus() == com.hotel.model.BookingStatus.RESERVED ? "PENDING" : booking.getStatus().name();
    }

    private long countOpenRequests(ServiceType serviceType) {
        return hotelService.getServiceRequests().stream()
                .filter(request -> request.getServiceType() == serviceType)
                .filter(request -> request.getStatus() != ServiceRequestStatus.COMPLETED)
                .count();
    }

    private String required(String value, String fieldName) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private <T> T required(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }

    private double parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Double.parseDouble(value.trim());
    }

    private void clearRoomForm() {
        roomNumberField.clear();
        roomPriceField.clear();
        roomTypeField.getSelectionModel().clearSelection();
    }

    private void clearBookingForm() {
        guestNameField.clear();
        guestPhoneField.clear();
        guestEmailField.clear();
        guestIdField.clear();
        serviceChargeField.clear();
        bookingNotesField.clear();
        bookingRoomField.getSelectionModel().clearSelection();
        bookingRoomField.getItems().clear();
        checkInField.setValue(null);
        checkOutField.setValue(null);
        checkOutField.setDisable(true);
    }

    private void configureBookingDatePickers() {
        checkInField.setValue(null);
        checkOutField.setValue(null);
        checkOutField.setDisable(true);
        bookingRoomField.setDisable(true);

        checkInField.setDayCellFactory(ignored -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });

        checkOutField.setDayCellFactory(ignored -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                LocalDate start = checkInField.getValue();
                setDisable(empty || start == null || !item.isAfter(start));
            }
        });

        checkInField.valueProperty().addListener((observable, oldValue, newValue) -> {
            checkOutField.setDisable(newValue == null);
            if (newValue == null || (checkOutField.getValue() != null && !checkOutField.getValue().isAfter(newValue))) {
                checkOutField.setValue(null);
            }
            updateAvailableRoomChoices();
        });

        checkOutField.valueProperty().addListener((observable, oldValue, newValue) -> updateAvailableRoomChoices());
    }

    private void updateAvailableRoomChoices() {
        LocalDate checkIn = checkInField.getValue();
        LocalDate checkOut = checkOutField.getValue();
        List<String> roomNumbers = hotelService.getAvailableRooms(checkIn, checkOut).stream()
                .map(Room::getRoomNumber)
                .toList();
        bookingRoomField.setItems(FXCollections.observableArrayList(roomNumbers));
        bookingRoomField.setDisable(checkIn == null || checkOut == null);
        if (!roomNumbers.contains(bookingRoomField.getValue())) {
            bookingRoomField.getSelectionModel().clearSelection();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Action failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
