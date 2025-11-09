package de.cyzetlc.hsbi.game.network.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class ClientLoginPacket extends Packet {
    @Getter @Setter
    private UUID client;

    public ClientLoginPacket(UUID client) {
        this.client = client;
    }

    @Override
    public void handle() {

    }
}
