module com.example.onlineauctionsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    exports com.example.onlineauctionsystem.model;
    opens com.example.onlineauctionsystem.model to javafx.fxml, javafx.base;

    opens com.example.onlineauctionsystem to javafx.fxml;
    exports com.example.onlineauctionsystem;

    opens com.example.onlineauctionsystem.controller.auth to javafx.fxml;
    exports com.example.onlineauctionsystem.controller.auth;

    opens com.example.onlineauctionsystem.controller to javafx.fxml;
    exports com.example.onlineauctionsystem.controller;

    opens com.example.onlineauctionsystem.controller.seller to javafx.fxml;
    exports com.example.onlineauctionsystem.controller.seller;

    opens com.example.onlineauctionsystem.controller.common to javafx.fxml;
    exports com.example.onlineauctionsystem.controller.common;

    opens com.example.onlineauctionsystem.controller.admin to javafx.fxml;
    exports com.example.onlineauctionsystem.controller.admin;
}