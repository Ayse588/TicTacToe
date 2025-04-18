module com.example.tictactoe {
        requires javafx.controls;
        requires javafx.fxml;


        opens com.example.tictactoe.controller to javafx.fxml;
        exports com.example.tictactoe;
        }