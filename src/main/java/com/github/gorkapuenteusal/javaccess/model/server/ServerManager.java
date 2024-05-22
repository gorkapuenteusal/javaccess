package com.github.gorkapuenteusal.javaccess.model.server;

import com.github.gorkapuenteusal.javaccess.utils.ServerFileManager;

import java.util.*;
import java.util.stream.Collectors;

public class ServerManager implements Iterable<Server> {
    private static final int FIRST_PORT = 50_500;
    private static ServerManager instance;
    private final List<Server> servers = new ArrayList<>();

    private ServerManager() {
    }

    public static synchronized ServerManager get() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    public void addServer(Server server) {
        server.listen();
        servers.add(server);
    }

    public void delete(Server server) {
        servers.remove(server);
        server.kill();
        ServerFileManager.get().remove(server.serialize());
    }

    public int getNextPort() {
        return FIRST_PORT + servers.size();
    }

    public void closeAllServers() {
        servers.forEach(server -> {
            ServerFileManager.get().write(server.serialize());
            server.kill();
        });
    }

    @Override
    public Iterator<Server> iterator() {
        return servers.iterator();
    }

    public String getAvailableId() {
        Set<String> existingIds = servers.stream()
                .map(Server::getId) // Assuming Server class has a getId() method
                .collect(Collectors.toSet());

        String newId;
        do {
            newId = generateRandomId();
        } while (existingIds.contains(newId)); // Ensure ID is unique

        return newId;
    }

    private String generateRandomId() {
        int length = 8; // Change as desired
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder id = new StringBuilder(length);

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            id.append(characters.charAt(random.nextInt(characters.length())));
        }

        return id.toString();
    }

    public void loadServers () {
        ServerFileManager.get().readAll().forEach( serializedServer -> {
            Server server = serializedServer.deserialize();
            if (server.isRunning()) server.listen();
            this.addServer(server);
        });
    }

    public Server getServer(int idx) {
        return servers.get(idx);
    }
}
