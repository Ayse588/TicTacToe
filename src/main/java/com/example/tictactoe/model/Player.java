package com.example.tictactoe.model;

/*
    * This enum represents the two players in the game: X and O.
    * It also includes an EMPTY state to represent an empty cell on the board.
    * Each player is represented by a string symbol.
    *
    */


public enum Player {
    X("X"),
    O("O"),
    EMPTY(" ");

    private final String symbol; //Speichert das Symbol


    Player(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }

    public Player opponent() {
        if (this == X) {
            return O;
        } else if (this == O) {
            return X;
        } else {
            return EMPTY;
        }
    }

}
