package de.cyzetlc.hsbi.game.listener;

import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.event.impl.ReceiveMessageEvent;
import de.cyzetlc.hsbi.game.event.impl.ReceivePacketEvent;
import de.cyzetlc.hsbi.game.network.packets.ClientLoginPacket;
import de.cyzetlc.hsbi.game.network.packets.Packet;
import de.cyzetlc.hsbi.game.network.packets.SerializationUtils;
import de.cyzetlc.hsbi.game.network.packets.UserMessagePacket;

import java.io.DataOutputStream;

public class PacketListener {
    @EventHandler
    // The `handleReceivePacket` method is a listener method that is called when a `ReceivePacketEvent` is fired. It takes
    // an instance of `ReceivePacketEvent` as a parameter, which contains information about the received packet and the
    // socket it was received from.
    public void handleReceivePacket(ReceivePacketEvent e) {
        Packet packet = e.getPacket();

        try {
            DataOutputStream dos = new DataOutputStream(e.getSocket().getOutputStream());

            if (packet instanceof UserMessagePacket messagePacket) {
                e.setCancelled(((EventCancelable)new ReceiveMessageEvent(messagePacket, e.getSocket()).call()).isCancelled());
            } else if (packet instanceof ClientLoginPacket clientLoginPacket) {
               // Server.MultiClientHandler.getClientLogger().info(clientLoginPacket.getClient().toString());
            } else {
                dos.write(SerializationUtils.serialize(new UserMessagePacket("Unable to resolve packet")));
                //Server.MultiClientHandler.getClientLogger().error("Unvalidated packet");
            }
        } catch (Exception ex) {
            //Server.getLogger().error(ex.getMessage());
        }
    }
}
