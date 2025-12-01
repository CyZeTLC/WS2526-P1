package de.cyzetlc.hsbi.listener;

import de.cyzetlc.hsbi.Server;
import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.event.impl.ReceiveMessageEvent;
import de.cyzetlc.hsbi.game.event.impl.ReceivePacketEvent;
import de.cyzetlc.hsbi.game.network.packets.*;

import java.io.DataOutputStream;
import java.io.IOException;

public class PacketListener {
    @EventHandler
    public void handleReceivePacket(ReceivePacketEvent e) {
        Packet packet = e.getPacket();

        try {
            DataOutputStream dos = new DataOutputStream(e.getSocket().getOutputStream());

            if (packet instanceof ClientSubmitScorePacket scorePacket) {
                // ... Daten speichern ...
                Server.getLogger().info("Got: " + scorePacket.getFinalScore());

                Server.MultiClientHandler handler = Server.findHandlerBySocket(e.getSocket());

                if (handler != null) {
                    // 2. Bestätigung zurücksenden
                    try {
                        handler.sendPacket(new UserMessagePacket("Score saved! Thank you."));

                        // Sende dem Client die aktuelle Top 10 Liste
                        // handler.sendPacket(new ServerSendHighscoresPacket(getTopTenScores()));

                    } catch (IOException ex) {
                        Server.getLogger().error("Failed to send response to client: " + ex.getMessage());
                    }
                }
            } else if (packet instanceof UserMessagePacket messagePacket) {
                e.setCancelled(((EventCancelable)new ReceiveMessageEvent(messagePacket, e.getSocket()).call()).isCancelled());
            } else if (packet instanceof ClientLoginPacket clientLoginPacket) {
                Server.MultiClientHandler.getClientLogger().info(clientLoginPacket.getClient().toString());
            } else if (packet instanceof ClientDataPacket clientDataPacket) {
                // send all players current Player data
            } else {
                dos.write(SerializationUtils.serialize(new UserMessagePacket("Unable to resolve packet")));
                Server.MultiClientHandler.getClientLogger().error("Unvalidated packet");
            }
        } catch (Exception ex) {
            Server.getLogger().error(ex.getMessage());
        }
    }
}
