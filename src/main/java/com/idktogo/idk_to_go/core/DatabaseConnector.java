package com.idktogo.idk_to_go.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnector {

    private static final String URL = "jdbc:mysql://turntable.proxy.rlwy.net:36473/railway";
    private static final String USER = "root";
    private static final String PASSWORD = "KpFTOLyHJewyyzItnculqsYCYuIGVAlJ";

    private DatabaseConnector() {}

    public static Connection getConnection() throws SQLException {
        // Always open a new connection (auto-managed by try-with-resources)
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
