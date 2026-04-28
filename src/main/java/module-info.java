module com.example.onlineauctionsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.onlineauctionsystem to javafx.fxml;
    opens com.example.onlineauctionsystem.controller to javafx.fxml;
    exports com.example.onlineauctionsystem.controller;
    exports com.example.onlineauctionsystem;
}