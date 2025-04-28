package com.example.tictactoe.model;

/**
 * This abstract class represents the model of a game.
 * It contains a GameState object that holds the current state of the game.
 * The class provides a method to get the current game state and an abstract method to reset the game.
 *
 */

public abstract class AbstractGameModel {
    protected GameState gameState;

    public synchronized GameState getGameState() {
        return gameState;
    }

    public abstract void resetGame();
}