package de.cyzetlc.hsbi.game.event.impl;

import de.cyzetlc.hsbi.game.network.packets.UserMessagePacket;

import java.net.Socket;

public class ReceiveMessageEvent extends ReceivePacketEvent {
    public ReceiveMessageEvent(UserMessagePacket packet, Socket socket) {
        super(packet, socket);
    }
}
