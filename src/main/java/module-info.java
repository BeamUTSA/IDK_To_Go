module com.idktogo.idk_to_go {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.prefs;
    requires java.sql;
    requires java.net.http;
    requires openai.java.core;
    requires openai.java.client.okhttp;
    requires org.json;


    opens com.idktogo.idk_to_go.controller to javafx.fxml;
    exports com.idktogo.idk_to_go;
}
