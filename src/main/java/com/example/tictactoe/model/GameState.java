package com.example.tictactoe.model;

/**
    * This enum represents the different states of the game.
    * It includes states for when the game is still playing, when player X or O wins, and when the game ends in a draw.
    * Each state is represented by a string message.
    *
    */


public enum GameState {
    PLAYING("Playing"),
    X_WINS("Player X wins!"),
    O_WINS("Player O wins!"),
    DRAW("It's a draw!");

    private final String message;

    /**
     * Constructor for the GameState enum.
     *
     * @param message the message associated with the game state
     */

    GameState(String message) {
        this.message = message;
    }

    /**
     * Returns the message associated with the game state.
     *
     * @return the message
     */

    public String getMessage() {
        return message;
    }

    public boolean isGameOver() {
        return this != PLAYING;
    }
}
