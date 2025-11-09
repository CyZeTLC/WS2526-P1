package de.cyzetlc.hsbi.game.event.impl;

import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.network.packets.Packet;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;

@Getter
public class ReceivePacketEvent extends EventCancelable {
    @Setter
    private Packet packet;

    private final Socket socket;

    public ReceivePacketEvent(Packet packet, Socket socket) {
        this.packet = packet;
        this.socket = socket;
    }
}
