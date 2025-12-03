package de.cyzetlc.hsbi.game.network.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class JoinCommunityPacket extends Packet {
    @Getter @Setter
    private UUID uuid;

    public JoinCommunityPacket(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void handle() {

    }
}
