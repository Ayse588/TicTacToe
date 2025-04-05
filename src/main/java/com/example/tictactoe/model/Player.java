package com.example.tictactoe.model;

public enum Player {
    X("X"),
    O("O"),
    EMPTY(" ");

    private final String symbol; //Speichert das Symbol

 // Konstruktor: Speichert das übergebene Symbol.
    private Player(String symbol) {
        this.symbol = symbol;
    }

    // Player test = new Player("X");
    // System.out.println(test.toString()); // "X"

    //Gibt "X", "O" oder " " zurück – gut für Ausgabe im UI
    @Override
    public String toString() {
        return symbol;
    }
//Gibt den Gegenspieler zurück (von X → O, von O → X, Empty bleibt Empty
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
