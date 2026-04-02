package com.hotel.service;

import com.hotel.model.HotelSnapshot;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {
    private static final Path BACKUP_DIR = Path.of("data", "backups");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public Path createBackup(HotelSnapshot snapshot) {
        try {
            Files.createDirectories(BACKUP_DIR);
            Path backupPath = BACKUP_DIR.resolve("hotel-backup-" + LocalDateTime.now().format(FORMATTER) + ".ser");
            try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(backupPath))) {
                outputStream.writeObject(snapshot);
            }
            return backupPath;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create backup", exception);
        }
    }
}
