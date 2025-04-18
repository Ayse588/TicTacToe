package com.example.tictactoe.network;

import com.example.tictactoe.model.Player;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkConnection {
    private final NetworkListener listener;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private volatile boolean running = false;
    private final String opponentIp;
    private final int port;
    private Player localPlayer = null;

//    new NetworkConnection(..., "127.0.1.0", 54321);

    public NetworkConnection(NetworkListener listener, String opponentIp, int port) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        this.listener = listener;
        this.opponentIp = opponentIp;
        this.port = port;
    }

    public void startServer() {
        if (running) {
            listener.onError("Server is already running");
            return;
        }
        this.localPlayer = Player.X;
        running = true;

        new Thread(() -> {
            try(ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("server started on port " + port + ". waiting for client...");
                socket = serverSocket.accept();
                System.out.println("client connected from " + socket.getInetAddress());
                setupStreams();
                startListening();
                Platform.runLater(() -> listener.onConnectionChanged(true, this.localPlayer));
            } catch (IOException e) {
                if (running) {
                    System.err.println("server error: " + e.getMessage());
                    Platform.runLater(() -> listener.onError("Server error: " + e.getMessage()));
                    closeConnection(false);
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("server error: " + e.getMessage());
                    Platform.runLater(() -> listener.onError("Server error: " + e.getMessage()));
                    closeConnection(false);
                }
            }
        }, "tictactoe_server_thread").start();
    }

    public void startClient() {
        if (running) {
            listener.onError("Client is already running");
            return;
        }
        this.localPlayer = Player.O;
        running = true;
        new Thread(() -> {
            try{
                System.out.println("attempting to connect to server" + opponentIp + ":" + port);
                socket = new Socket(opponentIp, port);
                System.out.println("connected to server");
                setupStreams();
                startListening();
                Platform.runLater(() -> listener.onConnectionChanged(true, this.localPlayer));
            } catch (UnknownHostException e) {
                System.err.println("client error: unknown host: " + e.getMessage());
                Platform.runLater(() -> listener.onError("unknown host: " + e.getMessage()));
                closeConnection(false);
            } catch (IOException e) {
                if (running) {
                    System.err.println("client error: " + e.getMessage());
                    Platform.runLater(() -> listener.onError("could not connect to server: " + opponentIp + ": error" + e.getMessage()));
                    closeConnection(false);
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("client error during startup: " + e.getMessage());
                    Platform.runLater(() -> listener.onError("client error: " + e.getMessage()));
                    closeConnection(false);
                }
            }
        }, "tictactoe_client_thread").start();
    }

    private void setupStreams() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("input/output streams created");
    }

    // client

    // a
    // b
    // c
    // \0

    // abc

    // server

    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                String messageFromServer;
                while (running && (messageFromServer = in.readLine()) != null) {
                    System.out.println("received message: " + messageFromServer);
                    processMessage(messageFromServer);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("network read error: " + e.getMessage());
                    Platform.runLater(() -> listener.onError("connection lost: " + e.getMessage()));
                    Platform.runLater(listener::onOpponentQuit);
                }
            } finally {
                if (running) {
                    closeConnection(false);
                }
            }
            System.out.println("listener thread finished");
        }, "tictactoe_networklistener_thread");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    // 15 2 10
    // 2:1

    private void processMessage(String message) {
        try {
            String[] parts = message.split(":", 2);
            NetworkCommand command = NetworkCommand.fromString(parts[0]);

            if (command == null) {
                System.err.println("unknown command: " + parts[0]);
                return;
            }

            switch (command) {
                case MOVE: {
                    if (parts.length == 2) {
                        String[] coords = parts[1].split(":");
                        if (coords.length == 2) {
                            int row = Integer.parseInt(coords[0]);
                            int col = Integer.parseInt(coords[1]);
                            Platform.runLater(() -> listener.onMoveReceived(row, col));
                        } else {
                            System.err.println("invalid format: " + message);
                        }
                    } else {
                        System.err.println("invalid format: " + message);
                    }
                    break;
                }
                case RESET: {
                    Platform.runLater(listener::onResetReceived);
                    break;
                }
                case QUIT: {
                    Platform.runLater(listener::onOpponentQuit);
                    closeConnection(false);
                    break;
                }
                default: {
                    System.err.println("unknown command: " + parts[0]);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("invalid format with coordinate: " + message + ": " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("error parsing message structure " + message);
        } catch (Exception e) {
            System.err.println("error parsing message " + message + ": " + e.getMessage());
            Platform.runLater(() -> listener.onError("error parsing message " + e.getMessage()));
        }
    }

    public synchronized boolean sendMessage(String message) {
        if (out != null && !socket.isClosed()) {
            System.out.println("sending message: " + message);
            out.println(message);
            return !out.checkError();
        } else {
            System.err.println("failed to send message, not connected");
            return false;
        }
    }

    public void sendMove(int row, int col) {
        sendMessage(NetworkCommand.MOVE + ":" + row + ":" + col);
    }

    public void sendReset() {
        sendMessage(NetworkCommand.RESET.toString());
    }

    public void sendQuitAndClose() {
        if (running) {
            sendMessage(NetworkCommand.QUIT.toString());
            closeConnection(false);
        }
    }

    public synchronized void closeConnection(boolean notifyOpponent) {
        if (!running) {
            return;
        }
        running = false;

        System.out.println("closing connection");

        if (notifyOpponent && out != null && socket != null && !socket.isClosed()) {
            out.println(NetworkCommand.QUIT.toString());
        }

        if (listenerThread != null) {
            listenerThread.interrupt();
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            System.err.println("error closing output stream: " + e.getMessage());
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.err.println("error closing input stream: " + e.getMessage());
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("error closing socket: " + e.getMessage());
        }

        out = null;
        in = null;
        socket = null;
        listenerThread = null;
        Player previouslyAssignedPlayer = this.localPlayer;
        this.localPlayer = null;

        Platform.runLater(() -> listener.onConnectionChanged(false, previouslyAssignedPlayer));
        System.out.println("connection closed");
    }

    public boolean isRunning() {
        return running && socket != null && socket.isConnected() && !socket.isClosed();
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

}
