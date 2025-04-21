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

/*
 * This class is the controller for the Tic Tac Toe game.
 * It handles the game logic, UI updates, and network communication.
 * It implements the NetworkListener interface to handle network events.
 */


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

    /*
        * This method loads the configuration from the config file.
        * It sets the opponent IP and network port based on the loaded properties.
        * If the properties are not found, it uses default values.
     */

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
    /*
        * This method creates the buttons for the game board.
        * It sets the properties of each button and adds them to the grid pane.
        * It also sets the action handler for each button.
     */

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

    /*
        * This method handles the click event on the game board buttons.
        * It checks if the move is valid and updates the game model accordingly.
        * It also updates the UI and sends the move over the network if applicable.
     */

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

    /*
        * This method updates the status label based on the current game state.
        * It checks if the game is over and updates the label accordingly.
        * It also handles the case where a network game is active.
     */

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

    /*
        * This method enables or disables the game board buttons based on the given parameter.
        * It iterates through all buttons and sets their disable property accordingly.
     */


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

    /*
        * This method is called when the host button is clicked.
        * It starts a server and waits for an opponent to connect.
        * It also handles the case where a network connection is already active.
     */

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

    /*
        * This method is called when the join button is clicked.
        * It attempts to connect to a host and starts a client.
        * It also handles the case where a network connection is already active.
     */

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

    /*
        * This method is called when the new game button is clicked.
        * It resets the game and updates the UI accordingly.
        * It also handles the case where a network connection is active.
     */

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

    /*
        * This method resets the game model and updates the UI accordingly.
        * It also handles the case where the reset is initiated over the network.
     */

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
    /*
        * This method updates the UI board based on the current state of the game model.
        * It sets the text and style of each button according to the player occupying that cell.
        * It also handles the case where a cell is empty.
     */

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

    /*
        * This method is called when a move is received from the opponent.
        * It updates the game model and the UI accordingly.
        * It checks if the move is valid and updates the game state.
     */
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

    /*
        * This method is called when a reset request is received from the opponent.
        * It resets the game model and updates the UI accordingly.
        * It also handles the case where a network connection is active.
     */

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

    /*
        * This method is called when the connection status changes.
        * It updates the UI and the game state accordingly.
        * It also handles the case where a network connection is active.
     */

    @Override
    public void onConnectionChanged(boolean connected, Player assignedPlayer) {
        System.out.println("networklistener: connection changed");
        isNetworkGame = connected;
        localPlayer = assignedPlayer;


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

    /*
        * This method is called when an error occurs during the network connection.
        * It updates the UI and the game state accordingly.
        * It also handles the case where a network connection is active.
     */

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

    /*
        * This method is called when the opponent quits the game.
        * It updates the UI and the game state accordingly.
        * It also handles the case where a network connection is active.
     */

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
