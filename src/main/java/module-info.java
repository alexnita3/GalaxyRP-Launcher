module com.galaxyrp.galaxyrplauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.api.services.drive;
    requires com.google.api.client;
    requires google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.auth;
    requires jdk.httpserver;

    opens com.galaxyrp.galaxyrplauncher to javafx.fxml;
    exports com.galaxyrp.galaxyrplauncher;
}