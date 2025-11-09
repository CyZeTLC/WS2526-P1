package de.cyzetlc.hsbi;

import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.event.EventManager;
import de.cyzetlc.hsbi.game.event.impl.ReceivePacketEvent;
import de.cyzetlc.hsbi.game.network.packets.Packet;
import de.cyzetlc.hsbi.game.network.packets.SerializationUtils;
import de.cyzetlc.hsbi.game.network.packets.UserMessagePacket;
import de.cyzetlc.hsbi.listener.PacketListener;
import de.cyzetlc.hsbi.listener.UserMessageListener;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Server {
    @Getter
    private final static Logger logger = LoggerFactory.getLogger(Server.class.getName());

    @Getter
    private static final List<MultiClientHandler> multiClientHandlerList = new LinkedList<>();

    /**
     * This function creates a server socket, registers event listeners, and accepts incoming client connections, assigning
     * each connection to a separate thread for handling.
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(25570);
        getLogger().info("ServerSocket connected: " + serverSocket);

        EventManager.register(new PacketListener());
        EventManager.register(new UserMessageListener());

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                getLogger().info("A new Client is connected: " + socket);

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                getLogger().info("Assigning new thread for this Client");

                Thread multiClientHandler = new MultiClientHandler(socket, dis, dos);
                multiClientHandlerList.add((MultiClientHandler) multiClientHandler);
                multiClientHandler.start();
            } catch (Exception e) {
                assert socket != null;
                socket.close();
                getLogger().error(e.getMessage());
            }
        }
    }

    public static class MultiClientHandler extends Thread {
        @Getter
        public static Logger clientLogger = LoggerFactory.getLogger(MultiClientHandler.class.getName());

        final DataInputStream dis;
        final DataOutputStream dos;
        final Socket socket;

        public MultiClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
            this.socket = s;
            this.dis = dis;
            this.dos = dos;
        }

        /**
         * This function continuously receives packets from a server, deserializes them, and checks if they should be
         * cancelled.
         */
        public void run() {
            byte[] received = new byte[500000]; // max: 0.5 MB
            while (true) {
                try {
                    dos.write(SerializationUtils.serialize(new UserMessagePacket("Connection is stable")));

                    // receiving packet
                    int bytesRead = dis.read(received);
                    byte[] actualReceivedData = new byte[bytesRead];
                    System.arraycopy(received, 0, actualReceivedData, 0, bytesRead);
                    Packet packet = SerializationUtils.deserialize(actualReceivedData, Packet.class);

                    if (((EventCancelable)new ReceivePacketEvent(packet, this.socket).call()).isCancelled()) {
                        break;
                    }

                } catch (Exception e) {
                    try {
                        getClientLogger().info("Client " + this.socket + " lost connection: " + e.getMessage());
                        this.socket.close();
                        break;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            try {
                this.dis.close();
                this.dos.close();
            } catch (IOException e) {
                getClientLogger().error(e.getMessage());
            }
            multiClientHandlerList.remove(this);
        }
    }
}

