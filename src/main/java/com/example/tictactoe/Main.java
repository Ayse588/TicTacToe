package com.example.tictactoe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("started application");

        try {
            String path = "/com/example/tictactoe/tictactoe.fxml";
            URL fxml = getClass().getResource(path);

            Parent root = FXMLLoader.load(fxml);

            Scene scene = new Scene(root);

            primaryStage.setTitle("tictactoe");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            primaryStage.show();
            System.out.println("application started successfully");
        } catch (IOException exception) {
            System.out.println("failed to load fxml" + exception);
            System.exit(1);
        } catch (Exception exception) {
            System.out.println("exception" + exception);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}