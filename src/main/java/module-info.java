module com.example.onlineauctionsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.onlineauctionsystem to javafx.fxml;
    exports com.example.onlineauctionsystem;
}