module com.example.tictactoe {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tictactoe to javafx.fxml;
    exports com.example.tictactoe;
    exports com.example.tictactoe.model;
    opens com.example.tictactoe.model to javafx.fxml;
}