module com.idktogo.idk_to_go {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql; // For JDBC / SQLite
    requires org.xerial.sqlitejdbc;
    requires java.prefs;
    requires java.desktop; // SQLite library module

    opens com.idktogo.idk_to_go to javafx.fxml;
    opens com.idktogo.idk_to_go.controller to javafx.fxml;

    exports com.idktogo.idk_to_go;
    exports com.idktogo.idk_to_go.controller;
}
