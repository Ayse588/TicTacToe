package com.example.tictactoe.controller;

import com.example.tictactoe.model.*;
import com.example.tictactoe.network.*;
import com.example.tictactoe.util.ConfigLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

import java.util.Properties;

public class GameController implements NetworkListener {
    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    @FXML private Button hostButton;
    @FXML private Button joinButton;
    @FXML private Button newGameButton;
    @FXML private Button quitButton;

    private static final int BOARD_SIZE = GameModel.getBoardSize();
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_OPPONENT_IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 54321;

    private GameModel gameModel;
    private Button[][] boardButtons;
    private NetworkConnection networkConnection;
    private Player localPlayer = null;

    private boolean myTurn = false;
    private boolean isNetworkGame = false;
    private String opponentIP;
    private int networkPort;

    // OXO
    // X O
    //  XX

    @FXML
    public void initialize() {
        System.out.println("initializing gamecontroller");
        gameModel = new GameModel();
        boardButtons = new Button[BOARD_SIZE][BOARD_SIZE];

        loadConfiguration();
        createBoardButtons();
        updateBoard();
        updateStatusLabel();
        enableDisableBoard(false);

        System.out.println("gamecontroller initialized successfully");

        Platform.runLater(() -> {
            if (boardGrid.getScene() != null && boardGrid.getScene().getWindow() != null) {
                boardGrid.getScene().getWindow().setOnCloseRequest(event -> {
                    cleanupAndExit();
                });
            } else {
                System.err.println("javafx application not initialized yet");
            }
        });

    }

    private void loadConfiguration() {
        Properties config = ConfigLoader.loadConfig(CONFIG_FILE);
        opponentIP = config.getProperty("opponent.ip", DEFAULT_OPPONENT_IP);
        try {
            networkPort = Integer.parseInt(config.getProperty("network.port", String.valueOf(DEFAULT_PORT)));
        } catch (NumberFormatException e) {
            System.out.println("invalid port number");
            networkPort = DEFAULT_PORT;
        }
        System.out.println("configuration loaded successfully: " + opponentIP + ":" + networkPort);
    }

    private void createBoardButtons() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Button button = new Button();
                button.setMinSize(90, 90);
                button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                button.setFont(new Font("Arial Bold", 36));
                final int r = row;
                final int c = col;

                button.setUserData(new int[]{r, c});
                button.setOnAction(this::handleCellClick);

                GridPane.setHalignment(button, HPos.CENTER);
                GridPane.setValignment(button, VPos.CENTER);
                GridPane.setHgrow(button, javafx.scene.layout.Priority.ALWAYS);
                GridPane.setVgrow(button, javafx.scene.layout.Priority.ALWAYS);

                boardGrid.add(button, col, row);
                boardButtons[row][col] = button;
            }
        }
        System.out.println("board buttons loaded successfully");
    }

    @FXML
    private void handleCellClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        int[] coords = (int[]) clickedButton.getUserData();
        int row = coords[0];
        int col = coords[1];

        System.out.println("button clicked: " + coords[0] + ", " + coords[1]);

        if (isNetworkGame && !myTurn) {
            System.out.println("not my turn");
            statusLabel.setText("waiting for opponent");
            return;
        }

        if (gameModel.getGameState().isGameOver()) {
            System.out.println("game is already over!");
            return;
        }

        if (gameModel.getPlayerAt(row, col) != Player.EMPTY) {
            System.out.println("field is already occupied");
            return;
        }

        boolean moveMade = false;
        try {
            moveMade = gameModel.makeMove(row, col);
        } catch (IllegalArgumentException e) {
            System.err.println("error while making move: " + e.getMessage());
            showAlert("Error", "invalid coordinates");
            return;
        }

        if (moveMade) {
            System.out.println("move was successful " + gameModel.getPlayerAt(row, col));
            updateBoard();
            updateStatusLabel();

            if (isNetworkGame && networkConnection != null && networkConnection.isRunning()) {
                System.out.println("sending move over network");
                networkConnection.sendMove(row, col);
                myTurn = false;
                enableDisableBoard(false);
                updateStatusLabel();
            } else if (!isNetworkGame) {
                enableDisableBoard(true);
                updateStatusLabel();
            }

            if (gameModel.getGameState().isGameOver()) {
                System.out.println("game is over: " + gameModel.getGameState().getMessage());
                enableDisableBoard(false);
                updateStatusLabel();
            }
        } else {
            System.out.println("move failed");
        }
    }

    private void updateStatusLabel() {
        GameState state = gameModel.getGameState();
        String statusText;

        if (state.isGameOver()) {
            statusText = state.getMessage();
        } else if (isNetworkGame) {
            if (networkConnection == null || !networkConnection.isRunning()) {
                statusText = "network disconnected";
            } else {
                statusText = "playing over network: you are " + localPlayer + ".";
                statusText += myTurn ? "your turn:" : "Opponent's turn (" + localPlayer + ")";
            }
        } else {
            statusText = "playing locally: player " + gameModel.getCurrentPlayer() + "'s turn.";
        }

        statusLabel.setText(statusText);
    }

    // OXO
    //  OX
    // X O

    private void enableDisableBoard(boolean enable) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (boardButtons[row][col] != null) {
                    boardButtons[row][col].setDisable(!enable);
                }
            }
        }
    }

    private void cleanupAndExit() {
        System.out.println("cleaning up and exiting");
        if (networkConnection != null) {
            networkConnection.sendQuitAndClose();
            networkConnection = null;
        }
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleHostGame() {
        System.out.println("host game button clicked");
        if (networkConnection != null && networkConnection.isRunning()) {
            showAlert("Network active", "a network connection is already active");
            return;
        }

        resetGameInternal(false);
        statusLabel.setText("Starting server, waiting for opponent to connect");
        isNetworkGame = true;
        localPlayer = Player.X;
        myTurn = true;
        networkConnection = new NetworkConnection(this, opponentIP, networkPort);
        networkConnection.startServer();

        hostButton.setDisable(true);
        joinButton.setDisable(true);
        enableDisableBoard(false);
    }

    @FXML
    private void handleJoinGame() {
        System.out.println("join game button clicked");
        if (networkConnection != null && networkConnection.isRunning()) {
            showAlert("Network active", "a network connection is already active");
            return;
        }

        resetGameInternal(false);
        statusLabel.setText("attempting to connect to host " + opponentIP + ":" + networkPort);
        isNetworkGame = true;
        localPlayer = Player.O;
        myTurn = false;
        networkConnection = new NetworkConnection(this, opponentIP, networkPort);
        networkConnection.startClient();

        hostButton.setDisable(true);
        joinButton.setDisable(true);
        enableDisableBoard(false);
    }

    @FXML
    private void handleNewGame() {
        System.out.println("new game button clicked");
        if (isNetworkGame && networkConnection != null && networkConnection.isRunning()) {
            System.out.println("sending RESET command to opponent");
            networkConnection.sendReset();
            resetGameInternal(true);
            myTurn = (localPlayer == Player.X);
            enableDisableBoard(myTurn && !gameModel.getGameState().isGameOver());
        } else {
            resetGameInternal(false);
            enableDisableBoard(true);
        }
        updateStatusLabel();
    }

    @FXML
    private void handleQuitGame() {
        System.out.println("quit game button clicked");
        cleanupAndExit();
    }

    private void resetGameInternal(boolean isNetworkReset) {
        System.out.println("resetting game internally. is network reset: " + isNetworkReset);
        gameModel.resetGame();
        updateBoard();

        if (!isNetworkReset) {
            isNetworkGame = false;
            localPlayer = null;
            myTurn = true;
            enableDisableBoard(true);

            hostButton.setDisable(false);
            joinButton.setDisable(false);
            if (networkConnection != null && networkConnection.isRunning()) {
                networkConnection.closeConnection(true);
                networkConnection = null;
            }
        } else {
            hostButton.setDisable(true);
            joinButton.setDisable(true);
        }
        updateStatusLabel();
    }

    //  XO
    // XXX
    // OO

    private void updateBoard() {
        Player[][] board = gameModel.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boardButtons[row][col].setText(board[row][col].toString());

                if (board[row][col] == Player.X) {
                    boardButtons[row][col].setStyle("-fx-text-fill: #00ffcb");
                } else if (board[row][col] == Player.O) {
                    boardButtons[row][col].setStyle("-fx-text-fill: #f61212");
                } else {
                    boardButtons[row][col].setStyle("-fx-text-fill: #1b1b1b");
                }
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void onMoveReceived(int row, int col) {
        System.out.println("received coordinates: row: " + row + " col: " + col);
        if (!isNetworkGame) return;

        if (gameModel.getCurrentPlayer() == localPlayer.opponent()) {
            boolean makeMove = gameModel.makeMove(row, col);
            if (makeMove) {
                updateBoard();
                updateStatusLabel();

                if (gameModel.getGameState().isGameOver()) {
                    myTurn = false;
                    enableDisableBoard(false);
                    System.out.println("networklistener: game is over");
                } else {
                    myTurn = true;
                    enableDisableBoard(true);
                    System.out.println("networklistener: it is now my turn");
                }
                updateStatusLabel();
            } else {
                System.err.println("received invalid move from opponent " + row + ", " + col);
                updateStatusLabel();
            }
        } else {
            System.err.println("received invalid move");
        }
    }

    @Override
    public void onResetReceived() {
        System.out.println("networklistener: new game requested");

        if (!isNetworkGame) return;

        showAlert("Game Reset", "Opponent requested a game reset");
        resetGameInternal(true);

        myTurn = (localPlayer == Player.X); // true
        enableDisableBoard(myTurn && !gameModel.getGameState().isGameOver());
        updateStatusLabel();
    }

    @Override
    public void onConnectionChanged(boolean connected, Player assignedPlayer) {
        System.out.println("networklistener: connection changed");
        isNetworkGame = connected;
        localPlayer = assignedPlayer;

        // (myTurn ? "your turn" : "opponent's turn")
//        String whoseTurn;
//        if (myTurn) {
//            whoseTurn = "your turn";
//        } else {
//            whoseTurn = "opponent's turn";
//        }

        if (connected) {
            this.localPlayer = assignedPlayer;
            myTurn = (localPlayer == Player.X);
            statusLabel.setText("connected as player " + localPlayer + ". " + (myTurn ? "your turn" : "opponent's turn"));
            hostButton.setDisable(true);
            joinButton.setDisable(true);
            enableDisableBoard(myTurn);
        } else {
            if (localPlayer != null) {
                showErrorAlert("Connection Lost", "disconnected from opponent");
            }
            resetGameInternal(false);
            statusLabel.setText("disconnected");
            hostButton.setDisable(false);
            joinButton.setDisable(false);
            enableDisableBoard(true);
            networkConnection = null;
        }
    }

    @Override
    public void onError(String message) {
        System.out.println("networklistener: error received: " + message);
        showErrorAlert("Network Error", message);

        if (networkConnection != null && !networkConnection.isRunning()) {
            isNetworkGame = false;
            localPlayer = null;
            myTurn = false;
            statusLabel.setText("network error, host/join again");
            hostButton.setDisable(false);
            joinButton.setDisable(false);
            enableDisableBoard(true);
            networkConnection = null;
        } else {
            updateStatusLabel();
        }
    }

    @Override
    public void onOpponentQuit() {
        System.out.println("networklistener: opponent quit");
        if (!isNetworkGame) return;

        showAlert("Opponent Quit", "Opponent has disconnected");
        resetGameInternal(false);
        statusLabel.setText("opponent quit, host/join again");
        hostButton.setDisable(false);
        joinButton.setDisable(false);
        enableDisableBoard(true);
        networkConnection = null;
    }
}
