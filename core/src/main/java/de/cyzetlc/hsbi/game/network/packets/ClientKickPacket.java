package de.cyzetlc.hsbi.game.network.packets;

import lombok.Getter;
import lombok.Setter;

public class ClientKickPacket extends Packet {
    @Getter @Setter
    private String message;

    public ClientKickPacket(String message) {
        this.message = message;
    }

    @Override
    public void handle() {

    }
}
