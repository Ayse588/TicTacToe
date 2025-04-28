package com.example.tictactoe.network;

/**
    * This enum represents the different commands that can be sent over the network.
    * Each command is represented by a string value.
    * The enum provides a method to convert a string to the corresponding command.
    *
 */

public enum NetworkCommand {
    MOVE("MOVE"),
    RESET("RESET"),
    QUIT("QUIT"),
    ERROR("ERROR"),
    CONNECT("CONNECT"),
    DISCONNECT("DISCONNECT");

    private final String command;


    NetworkCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

    /**
     * Converts a string to the corresponding NetworkCommand enum value.
     *
     * @param text the string to convert
     * @return the corresponding NetworkCommand enum value, or null if no match is found
     */


    public static NetworkCommand fromString(String text) {
        for (NetworkCommand cmd: NetworkCommand.values()) {
            if (cmd.command.equalsIgnoreCase(text)) {
                return cmd;
            }
        }
        return null;
    }

}