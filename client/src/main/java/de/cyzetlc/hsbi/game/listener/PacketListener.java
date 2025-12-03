package de.cyzetlc.hsbi.game.listener;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.event.impl.ReceiveMessageEvent;
import de.cyzetlc.hsbi.game.event.impl.ReceivePacketEvent;
import de.cyzetlc.hsbi.game.network.packets.*;
import de.cyzetlc.hsbi.game.world.Location;
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
            } else if (packet instanceof JoinCommunityPacket joinCommunityPacket) {
               // Server.MultiClientHandler.getClientLogger().info(clientLoginPacket.getClient().toString());
                Game.getLogger().info(joinCommunityPacket.getUuid() + " connected to CommunityServer!");

                EntityPlayer player = new EntityPlayer();
                player.setUuid(joinCommunityPacket.getUuid());
                player.setLocation(new Location());
                CommunityHandler.addPlayer(player);
            } else if (packet instanceof ClientDataPacket dataPacket) {
                //Game.getLogger().info("Updating community players");
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
