package de.cyzetlc.hsbi.game.network.packets;

import lombok.Getter;
import lombok.Setter;

public class UserMessagePacket extends Packet {
    @Getter @Setter
    private String message;

    public UserMessagePacket(String message) {
        this.message = message;
    }

    @Override
    public void handle() {

    }
}
