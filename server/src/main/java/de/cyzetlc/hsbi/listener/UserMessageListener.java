package de.cyzetlc.hsbi.listener;

import de.cyzetlc.hsbi.Server;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.event.impl.ReceiveMessageEvent;
import de.cyzetlc.hsbi.game.network.packets.UserMessagePacket;

import java.io.DataOutputStream;

public class UserMessageListener {
    @EventHandler
    // The `handleReceiveMessage` method is a listener method that is triggered when a `ReceiveMessageEvent` is fired. It
    // takes an instance of `ReceiveMessageEvent` as a parameter, which contains information about the event.
    public void handleReceiveMessage(ReceiveMessageEvent e) {
        UserMessagePacket packet = (UserMessagePacket) e.getPacket();

        try {
            DataOutputStream dos = new DataOutputStream(e.getSocket().getOutputStream());

            if (packet.getMessage().equals("Exit")) {
                e.getSocket().close();
                Server.MultiClientHandler.getClientLogger().info("Client " + e.getSocket() + " lost connection: Connection closed");
                e.setCancelled(true);
            } else if (packet.getMessage().equals("clients")) {
                dos.writeUTF("Connected clients: " + Server.getMultiClientHandlerList().size());
                Server.MultiClientHandler.getClientLogger().info(e.getSocket().getRemoteSocketAddress() + ": /" + packet.getMessage());
            } else {
                dos.writeUTF(packet.getMessage());
                Server.MultiClientHandler.getClientLogger().info(e.getSocket().getRemoteSocketAddress() + ": " + packet.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
