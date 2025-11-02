package com.idktogo.idk_to_go.core;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class EventLogger {
    private EventLogger() {}

    private static final Path DIR  = Path.of("data");
    private static final Path FILE = DIR.resolve("events.log");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static void ensure() throws IOException {
        if (!Files.exists(DIR)) Files.createDirectories(DIR);
        if (!Files.exists(FILE)) Files.createFile(FILE);
    }

    public static void log(String event, String detail) {
        try {
            ensure();
            try (FileWriter fw = new FileWriter(FILE.toFile(), true)) {
                String line = String.format("%s | %s | %s%n",
                        LocalDateTime.now().format(TS),
                        event == null ? "" : event,
                        detail == null ? "" : detail);
                fw.write(line);
            }
        } catch (IOException ignored) {
            // Intentionally fail-silent for UI stability
        }
    }

    // Convenience helpers
    public static void nav(String from, String to) {
        log("NAVIGATE", "from=" + from + " to=" + to);
    }

    public static void like(int userId, int restaurantId, boolean liked) {
        log("USER_INTERACT", "user=" + userId + " restaurant=" + restaurantId + " liked=" + liked);
    }
}
