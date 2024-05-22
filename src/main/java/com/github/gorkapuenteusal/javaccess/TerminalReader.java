package com.github.gorkapuenteusal.javaccess;

import com.github.gorkapuenteusal.javaccess.model.Command;

import java.io.BufferedInputStream;
import java.util.Scanner;

public class TerminalReader {
    private static TerminalReader instance;
    Scanner sc = new Scanner(new BufferedInputStream(System.in));

    private TerminalReader() {
    }

    public static synchronized TerminalReader get() {
        if (instance == null) instance = new TerminalReader();
        return instance;
    }

    public String read() {
        return sc.nextLine();
    }

    public int readInt() {
        return sc.nextInt();
    }

    public boolean readBoolean() {
        return sc.nextBoolean();
    }

    public void pressEnter() {
        System.out.println("Press enter");
        sc.nextLine();
    }

    public Command readCommand() {
        return Command.getCommand(read());
    }
}
