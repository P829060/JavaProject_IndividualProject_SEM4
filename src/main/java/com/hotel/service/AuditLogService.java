package com.hotel.service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditLogService {
    private static final Path LOG_PATH = Path.of("data", "audit.log");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLogService() {
        try {
            Files.createDirectories(LOG_PATH.getParent());
            if (!Files.exists(LOG_PATH)) {
                Files.createFile(LOG_PATH);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize audit log", exception);
        }
    }

    public synchronized void log(String action, String details) {
        String line = "%s | %s | %s%n".formatted(
                LocalDateTime.now().format(FORMATTER),
                action,
                details
        );
        try (RandomAccessFile file = new RandomAccessFile(LOG_PATH.toFile(), "rw")) {
            file.seek(file.length());
            file.write(line.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write audit log", exception);
        }
    }

    public synchronized List<String> readRecentEntries(int maxEntries) {
        if (maxEntries <= 0) {
            return List.of();
        }

        List<String> lines = new ArrayList<>();
        try (RandomAccessFile file = new RandomAccessFile(LOG_PATH.toFile(), "r")) {
            long pointer = file.length() - 1;
            StringBuilder builder = new StringBuilder();

            while (pointer >= 0 && lines.size() < maxEntries) {
                file.seek(pointer);
                int value = file.read();
                if (value == '\n') {
                    if (builder.length() > 0) {
                        lines.add(builder.reverse().toString().strip());
                        builder.setLength(0);
                    }
                } else if (value != '\r') {
                    builder.append((char) value);
                }
                pointer--;
            }

            if (builder.length() > 0 && lines.size() < maxEntries) {
                lines.add(builder.reverse().toString().strip());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read audit log", exception);
        }

        Collections.reverse(lines);
        return lines;
    }
}
