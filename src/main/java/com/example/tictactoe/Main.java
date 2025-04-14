package com.example.tictactoe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

//    private GameController controller;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("started application");

        try {
            URL fxml = getClass().getResource("tictactoe.fxml");
            if (fxml == null) {
                System.out.println("fxml file not found");
                System.exit(1);
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

//            controller = loader.getController();

            Scene scene = new Scene(root);

            primaryStage.setTitle("tictactoe");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            primaryStage.setOnCloseRequest(event -> {
                System.out.println("closed application");
//                if (controller != null) {
//                    controller.shutdown();
//                }
            });

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

    @Override
    public void stop() throws Exception {
        System.out.println("attempting to stop application");
//        if (controller != null) {
//            controller.shutdown();
//        }
        super.stop();
        System.out.println("application stopped");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
