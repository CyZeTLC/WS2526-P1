package de.cyzetlc.hsbi.network;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
public class ClientPlayer implements Serializable {
    @Serial
    private static final long serialVersionUID = 2234567L;

    private final UUID uuid;

    public ClientPlayer() {
        this.uuid = UUID.randomUUID();
    }
}
