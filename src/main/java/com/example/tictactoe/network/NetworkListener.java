package com.example.tictactoe.network;

import com.example.tictactoe.model.Player;

public interface NetworkListener {
    void onMoveReceived(int row, int col);

    void onResetReceived();

    void onConnectionChanged(boolean connected, Player assignedPlayer); // assignedPlayer zeigt an, ob der Spieler z. X oder O zugewiesen bekommen hat

    void onError(String message);

    void onOpponentQuit();
}
