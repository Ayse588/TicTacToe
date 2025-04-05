package com.example.tictactoe;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Hauptklasse für die Tic-Tac-Toe Anwendung.
 */
public class TicTacToeApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Tic-Tac-Toe Spiel");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("Tic-Tac-Toe");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
