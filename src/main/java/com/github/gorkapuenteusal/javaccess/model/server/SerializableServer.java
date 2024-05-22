package com.github.gorkapuenteusal.javaccess.model.server;

import java.io.Serializable;

public record SerializableServer(String id, String password, boolean running) implements Serializable {
    public Server deserialize() {
        return Server.ServerBuilder.create().setID(id).setPassword(password).setRunning(running).build();
    }
}
