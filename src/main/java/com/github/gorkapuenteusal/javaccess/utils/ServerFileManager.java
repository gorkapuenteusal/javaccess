package com.github.gorkapuenteusal.javaccess.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gorkapuenteusal.javaccess.model.server.SerializableServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerFileManager {
    private static ServerFileManager instance;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final String basePath;
    private ServerFileManager() {
        String userHome = System.getProperty("user.home");
        basePath = userHome + "/.javaccess/servers/";

        // Create directory if it does not exist
        File dir = new File(basePath);
        if (!dir.exists()) dir.mkdirs();
    }

    public static synchronized ServerFileManager get() {
        if (instance == null) instance = new ServerFileManager();
        return instance;
    }

    public void write(SerializableServer server) {
        try {
            File serverFile = new File(basePath + server.id() + ".json");
            objectMapper.writeValue(serverFile, server);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SerializableServer read(String id) {
        try {
            File serverFile = new File(basePath + id + ".json");
            if (!serverFile.exists()) {
                throw new RuntimeException("Server with ID " + id + " not found.");
            }
            return objectMapper.readValue(serverFile, SerializableServer.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading server with ID " + id, e);
        }
    }

    public List<SerializableServer> readAll() {
        List<SerializableServer> servers = new ArrayList<>();
        File dir = new File(basePath);

        // List all JSON files
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                try {
                    SerializableServer server = objectMapper.readValue(file, SerializableServer.class);
                    servers.add(server);
                } catch (IOException e) {
                    // Handle the exception if needed
                    System.err.println("Error reading file: " + file.getName());
                }
            }
        }

        return servers;
    }

    public void remove(SerializableServer serialize) {
        File serverFile = new File(basePath + serialize.id() + ".json");
        serverFile.delete();
    }
}
