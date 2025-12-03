package de.cyzetlc.hsbi.game.network.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PlayerListPacket extends Packet {
    @Getter @Setter
    private List<ClientDataPacket> dataPackets;

    public PlayerListPacket(List<ClientDataPacket> dataPackets) {
        this.dataPackets = dataPackets;
    }

    @Override
    public void handle() {

    }
}
