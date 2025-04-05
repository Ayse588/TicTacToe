package com.example.tictactoe.model;

public enum GameState {         //ein spezieller Datentyp, der eine feste Auswahl von Werten beschreibt
    PLAYING("Playing"),
    X_WINS("Player X wins!"),
    O_WINS("Player O wins!"),
    DRAW("It's a draw!");

    private final String message; //message speichert die Nachricht zum jeweiligen Zustand.

    private GameState(String message) { //Konstruktor für jeden GameState. Übergibt und speichert die passende Nachricht.
        this.message = message;
    }

    //Gibt die Nachricht für den jeweiligen Zustand zurück.
    public String getMessage() {
        return message;
    }

    //Gibt zurück, ob das Spiel vorbei ist (also nicht mehr PLAYING)
    public boolean isGameOver() {
        return this != PLAYING;
    }
}
