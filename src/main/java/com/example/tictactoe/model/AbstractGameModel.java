package com.example.tictactoe.model;

public abstract class AbstractGameModel {
    protected GameState gameState;

    public synchronized GameState getGameState() {
        return gameState;
    }

    public abstract void resetGame();
}