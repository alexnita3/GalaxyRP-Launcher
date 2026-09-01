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
    requires static lombok;

    opens com.galaxyrp.galaxyrplauncher to javafx.fxml;
    exports com.galaxyrp.galaxyrplauncher;
    exports com.galaxyrp.galaxyrplauncher.services;
    opens com.galaxyrp.galaxyrplauncher.services to javafx.fxml;
    exports com.galaxyrp.galaxyrplauncher.exceptions;
    opens com.galaxyrp.galaxyrplauncher.exceptions to javafx.fxml;
    exports com.galaxyrp.galaxyrplauncher.enums;
    opens com.galaxyrp.galaxyrplauncher.enums to javafx.fxml;
    exports com.galaxyrp.galaxyrplauncher.adapters;
    opens com.galaxyrp.galaxyrplauncher.adapters to javafx.fxml;
}