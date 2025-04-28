package com.example.tictactoe.model;

import java.util.Arrays;

/**
 * This class represents the game model for a Tic Tac Toe game.
 * It extends the AbstractGameModel class and implements the game logic.
 * The game board is represented as a 2D array of Player objects.
 * The current player is tracked, and the game state is updated based on player moves.
 * The class provides methods to reset the game, make moves, check for wins, and get the current state of the board.
 *
 */


public class GameModel extends AbstractGameModel {
    private static final int BOARD_SIZE = 3;
    private Player[][] board;
    private Player currentPlayer;


    /**
        * This constructor initializes the game board and sets the starting player to Player.X.
        * It also sets the game state to PLAYING.
        * The board is a 2D array of Player objects, initialized to EMPTY.
     */

    public GameModel() {
        this.board = new Player[BOARD_SIZE][BOARD_SIZE];
        resetGame();
    }

    /**
        * This constructor initializes the game board and sets the starting player.
        * It also sets the game state to PLAYING.
        * The starting player must be either Player.X or Player.O.
        * If an invalid player is provided, an IllegalArgumentException is thrown.
     */

    public GameModel(Player startingPlayer) {
        this();
        if (startingPlayer == Player.X || startingPlayer == Player.O) {
            this.currentPlayer = startingPlayer;
        } else {
            throw new IllegalArgumentException("Invalid starting player");
        }
        this.gameState = GameState.PLAYING;
    }

    /**
        * This method resets the game board to its initial state.
        * It sets all cells to EMPTY and sets the current player to Player.X.
        * The game state is also set to PLAYING.
     */
    @Override
    public synchronized void resetGame() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], Player.EMPTY);
        }
        currentPlayer = Player.X;
        gameState = GameState.PLAYING;
    }

    /**
     * This method is synchronized to ensure that only one thread can access it at a time.
     * It checks if the move is valid (within bounds and on an empty cell) and updates the board.
     * If the move is valid, it updates the game state and switches the current player.
     */


    public synchronized boolean makeMove(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            throw new IllegalArgumentException("wrong coordinates");
        }

        if (board[row][col] == Player.EMPTY && gameState == GameState.PLAYING) {
            board[row][col] = currentPlayer;
            updateGameState();
            if (gameState == GameState.PLAYING) {
                currentPlayer = currentPlayer.opponent();
            }
            return true;
        }
        return false;
    }

    /**
        * This method checks the current game state to determine if there is a winner or if the game is a draw.
        * It checks all possible winning combinations (rows, columns, diagonals) for the current player.
     */

    private void updateGameState() {
        if (checkWin(currentPlayer) == true) {
            gameState = (currentPlayer == Player.X) ? GameState.X_WINS : GameState.O_WINS;
        } else if (isBoardFull()) {
            gameState = GameState.DRAW;
        } else {
            gameState = GameState.PLAYING;
        }
    }

    /**
        * This method checks if the current player has won the game by checking all possible winning combinations.
        * It returns true if the player has won, false otherwise.
     */

    private boolean checkWin(Player player) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return true;
        }

        for (int j = 0; j < BOARD_SIZE; j++) {
            if (board[0][j] == player && board[1][j] == player && board[2][j] == player) return true;
        }

        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return true; //überprüft beide Diagonalen
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) return true;

        return false; //wenn keine Bedingung erfüllt ist - hat der Spieler nicht gewonnen
    }


    /**
        * This method checks if the board is full (i.e., there are no empty cells left).
        * It returns true if the board is full, false otherwise.
     */

    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == Player.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
        * This method returns a copy of the current game board.
        * It is synchronized to ensure thread safety when accessing the board.
        * The returned board is a 2D array of Player objects representing the current state of the game.
        * This allows external code to access the board without modifying it directly.
     */

    public synchronized Player[][] getBoard() {   // Gibt eine Kopie des Spielfelds zurück, damit es von außen nicht versehentlich verändert wird
        Player[][] boardCopy = new Player[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            boardCopy[i] = Arrays.copyOf(this.board[i], BOARD_SIZE);
        }
        return boardCopy;
    }

    public synchronized Player getCurrentPlayer() {
        return currentPlayer;
    }

    public synchronized Player getPlayerAt(int row, int col) {
        return board[row][col];
    }

    public static int getBoardSize() {
        return BOARD_SIZE;
    }
}
