package de.cyzetlc.hsbi.game.network.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class ClientDataPacket extends Packet {
    @Getter @Setter
    private UUID client;

    public ClientDataPacket(UUID client) {
        this.client = client;
    }

    @Override
    public void handle() {

    }
}
