package de.cyzetlc.hsbi.game.listener;

import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.event.impl.ReceiveMessageEvent;
import de.cyzetlc.hsbi.game.event.impl.ReceivePacketEvent;
import de.cyzetlc.hsbi.game.network.packets.*;
import de.cyzetlc.hsbi.network.CommunityHandler;

import java.io.DataOutputStream;

public class PacketListener {
    @EventHandler
    public void handleReceivePacket(ReceivePacketEvent e) {
        Packet packet = e.getPacket();

        try {
            DataOutputStream dos = new DataOutputStream(e.getSocket().getOutputStream());

            if (packet instanceof UserMessagePacket messagePacket) {
                e.setCancelled(((EventCancelable)new ReceiveMessageEvent(messagePacket, e.getSocket()).call()).isCancelled());
            } else if (packet instanceof ClientLoginPacket clientLoginPacket) {
               // Server.MultiClientHandler.getClientLogger().info(clientLoginPacket.getClient().toString());
                EntityPlayer player = new EntityPlayer();
                player.setUuid(clientLoginPacket.getClient());
                CommunityHandler.addPlayer(player);
            } else if (packet instanceof ClientDataPacket dataPacket) {
                CommunityHandler.updatePlayerData(dataPacket);
            } else {
                dos.write(SerializationUtils.serialize(new UserMessagePacket("Unable to resolve packet")));
                //Server.MultiClientHandler.getClientLogger().error("Unvalidated packet");
            }
        } catch (Exception ex) {
            //Server.getLogger().error(ex.getMessage());
        }
    }
}
