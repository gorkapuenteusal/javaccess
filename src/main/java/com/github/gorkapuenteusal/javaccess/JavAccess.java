package com.github.gorkapuenteusal.javaccess;

import com.github.gorkapuenteusal.javaccess.model.Command;
import com.github.gorkapuenteusal.javaccess.model.server.Server;
import com.github.gorkapuenteusal.javaccess.model.server.ServerManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

public class JavAccess {

    /**
     * The application's entry point
     *
     * @param args no use
     */
    public static void main(String[] args) {
        ServerManager.get().loadServers();
        System.out.println("JavAccess - 2024 (c) Gorka Puente Díez");
        boolean exit = false;
        while (!exit) {
            System.out.println("Options: server manager, request connection, exit");
            String input = TerminalReader.get().read();
            ;
            switch (input) {
                case "server manager" -> serverManager();
                case "request connection" -> requestConnection();
                case "exit" -> exit = true;
                case null, default -> {
                }
            }
        }
        System.exit(0);
    }

    private static void requestConnection() {
        System.out.print("Enter the ip: ");
        String ip = TerminalReader.get().read();
        System.out.print("Enter the port: ");
        int port = TerminalReader.get().readInt();

        try {
            Socket socket = new Socket(ip, port);
            System.out.println("Connected to the server at " + ip + " on port " + port);
            TerminalReader.get().pressEnter();

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.print("Enter the server password: ");
            String password = TerminalReader.get().read();

            out.println(Command.PASSWORD.send(password));

            String commandAndArgs;
            Command command;
            String[] args;

            commandAndArgs = in.readLine();
            command = Command.getCommand(commandAndArgs);
            if (command == Command.INIT) {
                System.out.println("Sesion de terminal iniciada.");
                boolean sessionActive = true;
                while (sessionActive) {
                    System.out.print("Enter command: ");
                    String userCommand = TerminalReader.get().read();
                    out.println(userCommand);

                    if ("exit".equalsIgnoreCase(userCommand)) {
                        sessionActive = false;
                    } else {
                        String response;
                        while ((response = in.readLine()) != null && !response.equals("END_OF_COMMAND_OUTPUT")) {
                            System.out.println(response);
                        }
                    }
                }
            }

            // Close the streams and the socket
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void serverManager() {
        boolean exit = false;
        while (!exit) {
            System.out.println("Options: add, delete, exit");
            Iterator<Server> itr = ServerManager.get().iterator();
            itr.forEachRemaining(System.out::println);
            String input = TerminalReader.get().read();

            switch (input) {
                case "add" -> addServer();
                case "delete" -> deleteServer();
                case "exit" -> exit = true;
                case null, default -> {
                }
            }
        }
        ServerManager.get().closeAllServers();
    }

    private static void deleteServer() {
        int idx = 1;
        for (Server server : ServerManager.get()) {
            System.out.println(idx++ + ". " + server);
        }
        System.out.println("Select one");
        int input = TerminalReader.get().readInt();
        if (input < 1 || input >= idx) return;
        ServerManager.get().delete(ServerManager.get().getServer(input - 1));
    }

    private static void addServer() {
        Server.ServerBuilder builder = Server.ServerBuilder.create();
        boolean exit = false;
        boolean add = false;
        while (!exit) {
            System.out.println("Options: password, running, id, cancel, save.");
            Server possibleServer = builder.build();
            add = possibleServer != null;
            System.out.println(add ? possibleServer : "Invalid server");
            String input = TerminalReader.get().read();
            switch (input) {
                case "password" -> {
                    String password = TerminalReader.get().read();
                    if (password == null || password.isBlank()) break;
                    builder.setPassword(password);
                }
                case "running" -> {
                    boolean running = TerminalReader.get().readBoolean();
                    builder.setRunning(running);
                }
                case "id" -> {
                    String id = TerminalReader.get().read();
                    if (!id.matches("[a-zA-Z0-9]+")) break;
                    builder.setID(id);
                }
                case "cancel" -> {
                    add = false;
                    exit = true;
                }
                case "save" -> {
                    if (!add) break;
                    exit = true;
                }
                case null, default -> {
                }
            }
        }
        if (add) {
            Server server = builder.build();
            server.listen();
            ServerManager.get().addServer(server);
        }
    }
}
