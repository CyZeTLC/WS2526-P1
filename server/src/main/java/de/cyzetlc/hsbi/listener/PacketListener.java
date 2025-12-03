package de.cyzetlc.hsbi.listener;

import de.cyzetlc.hsbi.Server;
import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.event.impl.ReceiveMessageEvent;
import de.cyzetlc.hsbi.game.event.impl.ReceivePacketEvent;
import de.cyzetlc.hsbi.game.network.packets.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PacketListener {
    private HashMap<Socket, UUID> insideCommunity = new HashMap<>();

    @EventHandler
    public void handleReceivePacket(ReceivePacketEvent e) {
        Packet packet = e.getPacket();
        try {
            DataOutputStream dos = new DataOutputStream(e.getSocket().getOutputStream());

            if (packet instanceof ClientSubmitScorePacket scorePacket) {
                // ... Daten speichern ...
                Server.getLogger().info("Got: " + scorePacket.getFinalTime());

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
            } else if (packet instanceof JoinCommunityPacket communityPacket) {
                if (!this.insideCommunity.containsValue(communityPacket.getUuid())) {
                    this.insideCommunity.put(e.getSocket(), communityPacket.getUuid());
                    Server.getLogger().info(e.getSocket().getInetAddress().getHostName() + " connected to community");
                }

                for (Socket socket : this.insideCommunity.keySet()) {
                    if (socket != e.getSocket()) {
                        Server.MultiClientHandler handler = Server.findHandlerBySocket(socket);

                        if (handler != null) {
                            //handler.sendPacket(communityPacket);
                        }
                    }
                }
            } else if (packet instanceof UserMessagePacket messagePacket) {
                e.setCancelled(((EventCancelable)new ReceiveMessageEvent(messagePacket, e.getSocket()).call()).isCancelled());
            } else if (packet instanceof ClientLoginPacket clientLoginPacket) {
                Server.MultiClientHandler.getClientLogger().info(clientLoginPacket.getClient().toString());
            } else if (packet instanceof ClientDataPacket clientDataPacket) {
                for (Socket socket : this.insideCommunity.keySet()) {
                    if (socket != e.getSocket()) {
                        Server.MultiClientHandler handler = Server.findHandlerBySocket(socket);

                        if (handler != null) {
                            handler.sendPacket(clientDataPacket);
                        }
                    }
                }
            } else {
                //dos.write(SerializationUtils.serialize(new UserMessagePacket("Unable to resolve packet")));
                Server.MultiClientHandler.getClientLogger().error("Unvalidated packet");
            }
        } catch (Exception ex) {
            Server.getLogger().error(ex.getMessage());
        }
    }
}
