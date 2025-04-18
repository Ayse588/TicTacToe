package com.example.tictactoe.model;

import java.util.Arrays;

//Konstante für die Spielfeldgröße (3x3 Felder)
public class GameModel {
    private static final int BOARD_SIZE = 3;  //final=unveränderlich
    private Player[][] board;  //2D-Array (3x3) vom Typ Player. Enthält für jedes Feld den aktuellen Zustand: X, O oder leer.
    private Player currentPlayer; //Speichert, welcher Spieler aktuell am Zug ist.
    private GameState gameState; //Speichert den aktuellen Spielstatus (läuft noch, X gewinnt, O gewinnt, Unentschieden).


    public GameModel() {
        this.board = new Player[BOARD_SIZE][BOARD_SIZE];
        resetGame(); //Ruft resetGame() auf, um alle Felder auf leer zu setzen und das Spiel vorzubereiten.
    }

    public GameModel(Player startingPlayer) {
        this(); //Erzeugt erst das leere Spielfeld und ruft resetGame()
        if (startingPlayer == Player.X || startingPlayer == Player.O) {
            this.currentPlayer = startingPlayer;
        } else {
            throw new IllegalArgumentException("Invalid starting player");
        }
        this.gameState = GameState.PLAYING; //Das Spiel beginnt im Zustand PLAYING.
    }

    public synchronized void resetGame() {  //Durchläuft alle Zeilen (i) und setzt jedes Feld in der Zeile mit Player.EMPTY.
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], Player.EMPTY);
        }
        currentPlayer = Player.X; //Setzt Spieler X als Startspieler.
        gameState = GameState.PLAYING; //Setzt den Spielstatus zurück auf "läuft noch".
    }

    // XXO // reihe 0
    // OOX // reihe 1
    // X X // reihe 2


    // XXX
    // OXO
    // OOX

    public synchronized boolean makeMove(int row, int col) {  //Methode zum Ausführen eines Spielzugs an Position (row, col).
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            throw new IllegalArgumentException("wrong coordinates");  //Prüft, ob die Koordinaten im gültigen Bereich liegen (0 bis 2).
        }

        if (board[row][col] == Player.EMPTY && gameState == GameState.PLAYING) { //Nur möglich, wenn das Feld leer ist und das Spiel noch läuft.
            board[row][col] = currentPlayer; //Setzt den aktuellen Spieler auf das Feld.
            updateGameState(); //Prüft danach, ob jemand gewonnen hat oder das Spiel vorbei ist.
            if (gameState == GameState.PLAYING) {
                currentPlayer = currentPlayer.opponent();
            }
            return true;
        }
        return false;
    }

    private void updateGameState() {
        if (checkWin(currentPlayer) == true) {
            gameState = (currentPlayer == Player.X) ? GameState.X_WINS : GameState.O_WINS;
        } else if (isBoardFull()) {
            gameState = GameState.DRAW;
        } else {
            gameState = GameState.PLAYING;
        }
    }

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

    // XXO // reihe 0
    // OOX // reihe 1
    // XOX // reihe 2

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

    public GameState getGameState() {
        return gameState;
    }
}
