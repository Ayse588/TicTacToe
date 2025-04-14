package com.example.tictactoe.network;

public enum NetworkCommand {
    MOVE("MOVE"),
    RESET("RESET"),
    QUIT("QUIT"),
    ERROR("ERROR"),
    CONNECT("CONNECT"),
    DISCONNECT("DISCONNECT");

    private final String command;

//    new NetworkCommand();

    NetworkCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

    // test = lowercase
    // TEST = uppercase
    //


    public static NetworkCommand fromString(String text) {
        for (NetworkCommand cmd: NetworkCommand.values()) {
            if (cmd.command.equalsIgnoreCase(text)) {
                return cmd;
            }
        }
        return null;
    }

}

// XOX
// XOX
// OO