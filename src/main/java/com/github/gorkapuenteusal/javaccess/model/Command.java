package com.github.gorkapuenteusal.javaccess.model;

import java.util.Arrays;

public enum Command {
    PASSWORD,
    WRONG,
    INIT, SHELL, EXIT;

    public String send(String... args) {
        StringBuilder stringBuilder = new StringBuilder(this.toString());
        for (String arg : args) {
            stringBuilder.append(", ").append(arg);
        }
        return stringBuilder.toString();
    }

    public static Command getCommand(String commandAndArguments) {
        String command = commandAndArguments.split(", ")[0];
        return switch (command) {
            case "PASSWORD" -> PASSWORD;
            case "INIT" -> INIT;
            case "EXIT" -> EXIT;
            case "SHELL" -> SHELL;
            case null, default -> WRONG;
        };
    }

    public static String[] getArgs(String commandAndArguments) {
        String[] args = commandAndArguments.split(", ");
        if (args.length == 1)
            return new String[0];

        return Arrays.copyOfRange(args, 1, args.length);
    }
}
