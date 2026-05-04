module com.example.onlineauctionsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.net.http;


    opens com.example.onlineauctionsystem to javafx.fxml;
    opens com.example.onlineauctionsystem.controller.auth to javafx.fxml;
    exports com.example.onlineauctionsystem.controller.auth;
    exports com.example.onlineauctionsystem;
}