package com.idktogo.idk_to_go.data;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;

public final class SQLiteManager {

    private static final String DB_PATH = "data/idktogo.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    // Shared connection cache
    private static Connection sharedConnection;

    // Avoid repeated schema setup
    private static boolean schemaInitialized = false;

    private SQLiteManager() {}

    // === PUBLIC API ===

    /**
     * Main entry point for DAO/service classes.
     * Returns a shared SQLite connection (single instance for app lifecycle).
     */
    public static synchronized Connection getConnection() {
        try {
            if (sharedConnection == null || sharedConnection.isClosed()) {
                sharedConnection = DriverManager.getConnection(URL);
                enableForeignKeys(sharedConnection);
                initializeSchemaIfNeeded(sharedConnection);
                System.out.println("SQLite connected @ " + DB_PATH);
            }
        } catch (SQLException e) {
            System.err.println("SQLite connection failed: " + e.getMessage());
        }
        return sharedConnection;
    }

    // === INTERNAL HELPERS ===

    private static void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    /**
     * Loads and executes the schema from /resources/schema.sqlite.sql.
     */
    private static synchronized void initializeSchemaIfNeeded(Connection conn) {
        if (schemaInitialized) return;
        runSchemaDDLFromResource(conn, "schema.sqlite.sql");
        schemaInitialized = true;
    }

    /**
     * Reads an SQL script from the classpath and executes all statements.
     */
    private static void runSchemaDDLFromResource(Connection conn, String resourceName) {
        try (InputStream is = SQLiteManager.class.getResourceAsStream("/" + resourceName)) {
            if (is == null) {
                throw new RuntimeException("Missing schema file: " + resourceName);
            }
            String ddl = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            try (Statement stmt = conn.createStatement()) {
                for (String line : ddl.split(";")) {
                    if (!line.isBlank()) stmt.execute(line.trim());
                }
            }
            System.out.println("Loaded schema: " + resourceName);
        } catch (Exception e) {
            throw new RuntimeException("Schema initialization failed: " + e.getMessage(), e);
        }
    }

    /** Closes any AutoCloseable quietly (used in DAOs if needed) */
    public static void closeQuietly(AutoCloseable c) {
        if (c != null)
            try { c.close(); } catch (Exception ignored) {}
    }
}
