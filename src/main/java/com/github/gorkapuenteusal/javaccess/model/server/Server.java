package com.github.gorkapuenteusal.javaccess.model.server;

import com.github.gorkapuenteusal.javaccess.model.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {
    private static final int TIME_OUT_MS = 500;
    private final String id;
    private final int port;
    private final String password;
    private boolean running;
    private ServerSocket socket;

    private Server(String id, int port, String password, boolean running) {
        this.id = id;
        this.port = port;
        this.password = password;
        this.running = running;
        if (running) listen();
    }

    public void listen() {
        new Thread(() -> {
            try {
                socket = new ServerSocket(port);
                socket.setSoTimeout(TIME_OUT_MS);
                while (running) {
                    try (Socket clientSocket = socket.accept()) {
                        // TODO: handle clientSocket
                        System.out.println("Client connected: " + clientSocket);

                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        String commandAndArgs;
                        Command command;
                        String[] args;

                        commandAndArgs = in.readLine();
                        if (Command.getCommand(commandAndArgs) == Command.PASSWORD) {
                            args = Command.getArgs(commandAndArgs);
                            if (args.length == 1 && password.equals(args[0])) {
                                System.out.println("Client accessed correctly with password");
                                out.println(Command.INIT);

                                boolean sessionActive = true;
                                while (sessionActive) {
                                    commandAndArgs = in.readLine();
                                    System.out.println("Command: \"" + commandAndArgs + "\" arrived.");
                                    if (commandAndArgs == null || "exit".equalsIgnoreCase(commandAndArgs)) {
                                        sessionActive = false;
                                    } else {
                                        String[] shellCommand = {"sh", "-c", commandAndArgs};
                                        try {
                                            Process process = Runtime.getRuntime().exec(shellCommand);
                                            BufferedReader processOutput = new BufferedReader(
                                                    new InputStreamReader(process.getInputStream())
                                            );
                                            BufferedReader processError = new BufferedReader(
                                                    new InputStreamReader(process.getErrorStream())
                                            );
                                            String line;
                                            while ((line = processOutput.readLine()) != null) {
                                                out.println(line);
                                            }
                                            while ((line = processError.readLine()) != null) {
                                                out.println(line);
                                            }
                                            processOutput.close();
                                            processError.close();
                                        } catch (IOException e) {
                                            out.println("Error executing command: " + e.getMessage());
                                        }
                                        out.println("END_OF_COMMAND_OUTPUT");
                                    }
                                }
                            } else out.println(Command.WRONG);
                        } else out.println(Command.WRONG);

                        // Close streams
                        in.close();
                        out.close();
                    } catch (SocketTimeoutException ignored) {}
                }
                socket.close();
            } catch (IOException ignored) {

            }
        }).start();
    }

    public synchronized void kill() {
        running = false;
    }

    @Override
    public String toString() {
        return "Server \"" + id + "\" in port " + port + " <" + (running? "running": "stopped") + ">";
    }

    public void toggle() {
        if (running) kill();
        else listen();
    }

    public SerializableServer serialize() {
        return new SerializableServer(id, password, running);
    }

    public boolean matchesPassword(String password) {
        return this.password.equals(password);
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRunning() {
        return running;
    }

    public static class ServerBuilder {
        private String password;
        private String id;
        private boolean running = false;

        private ServerBuilder() {

        }

        public static ServerBuilder create() {
            return new ServerBuilder();
        }

        public ServerBuilder setPassword(String password) throws IllegalArgumentException {
            if (password == null || password.isBlank())
                throw new IllegalArgumentException("The password cannot be empty!");
            this.password = password;
            return this;
        }

        public ServerBuilder setID(String id) throws IllegalArgumentException {
            if (!id.matches("[a-zA-Z0-9]+"))
                throw new IllegalArgumentException("The ID must be alphanumeric!");
            this.id = id;
            return this;
        }

        public ServerBuilder setRunning(boolean running) {
            this.running = running;
            return this;
        }

        public Server build() {
            if (password == null)
                return null;

            return new Server(
                    id == null? ServerManager.get().getAvailableId() : id,
                    ServerManager.get().getNextPort(),
                    password,
                    running
            );
        }
    }
}
