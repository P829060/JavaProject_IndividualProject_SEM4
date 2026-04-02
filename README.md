# Hotel Management Application

Simple JavaFX hotel management system created for the Week 10 project.

## Included Features

- Login screen
- Dashboard with live hotel summary cards
- Room management
- Booking and guest management
- Billing and payment tracking
- Dedicated service request management with queue visibility and status tracking
- Booking search and room amenity lookup
- Report export, serialized backup, and recent audit log view
- Permanent file-based storage in the `data/` folder
- Multiple JavaFX layouts and controls using FXML
- CSS styling for a neat presentation

## Run

1. Open the project in IntelliJ IDEA or another IDE with Maven support.
2. Let the IDE import dependencies from `pom.xml`.
3. Run `com.hotel.HotelApplication`.

If Maven is installed locally, you can also use:

```powershell
mvn javafx:run
```

With your local JavaFX SDK, this also compiles from terminal:

```powershell
javac --module-path "C:\Users\pvpmo\OneDrive\Desktop\JavaFX\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d out-compile (Get-ChildItem -Recurse src\main\java -Filter *.java | ForEach-Object { $_.FullName })
```

## Demo Login

- Username: `admin`
- Password: `admin123`

## Concepts Covered

- JavaFX stages, scenes, controls, and event handling
- FXML-based UI design
- Layout panes like `BorderPane`, `GridPane`, `HBox`, `VBox`, `AnchorPane`
- TableView, ComboBox, DatePicker, MenuBar, Dialog/Alert usage
- File handling with permanent storage
- Character-stream report export
- Random access file-based audit log
- Serialization backup support
- Object-oriented design with models, services, and controllers
- Billing calculation and reporting workflow
- `RoomType` enum with constructor and methods for tariff calculation
- Wrapper classes with autoboxing and unboxing in the billing calculator
- Abstraction and interface-based design for room amenities
- Multithreaded service processing with up to 3 concurrent requests per service type
- Earlier-lab OSDL concepts are integrated into the application logic rather than shown as a separate demo screen
- Generic filtering is used for booking search
