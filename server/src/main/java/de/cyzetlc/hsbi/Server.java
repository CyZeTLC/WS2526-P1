package de.cyzetlc.hsbi;

import de.cyzetlc.hsbi.game.utils.json.JsonConfig;
import de.cyzetlc.hsbi.game.utils.json.database.mysql.MySQLCredentials;
import de.cyzetlc.hsbi.game.utils.json.database.mysql.QueryHandler;
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

    @Getter
    private static Server instance;

    @Getter
    private QueryHandler queryHandler;

    @Getter
    private static JsonConfig config;

    public Server() throws IOException {
        instance = this;

        getLogger().info("Loading configuration..");

        config = new JsonConfig("./config.json");

        getLogger().info("Configuration loaded successfully!");

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

    /**
     * This function creates a server socket, registers event listeners, and accepts incoming client connections, assigning
     * each connection to a separate thread for handling.
     */
    public static void main(String[] args) throws IOException {
        new Server();
    }

    /**
     * Finds the MultiClientHandler instance associated with the given Socket.
     * @param socket The socket of the client.
     * @return The corresponding MultiClientHandler, or null if not found.
     */
    public static MultiClientHandler findHandlerBySocket(Socket socket) {
        for (MultiClientHandler handler : multiClientHandlerList) {
            if (handler.getSocket().equals(socket)) {
                return handler;
            }
        }
        return null; // Handler nicht gefunden
    }

    public static class MultiClientHandler extends Thread {
        @Getter
        public static Logger clientLogger = LoggerFactory.getLogger(MultiClientHandler.class.getName());

        final DataInputStream dis;
        final DataOutputStream dos;

        @Getter
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

        /**
         * Serializes and sends a Packet object to the connected client.
         * @param packet The Packet to be sent.
         * @throws IOException If an error occurs during serialization or writing to the stream.
         */
        public void sendPacket(Packet packet) throws IOException {
            byte[] bytes = SerializationUtils.serialize(packet);
            this.dos.write(bytes);
            this.dos.flush();

            clientLogger.info("Sent packet type: " + packet.getClass().getSimpleName() + " to " + this.socket);
        }
    }

    /**
     * It creates a new QueryHandler object with the credentials from the config file, and then creates a table if it
     * doesn't exist
     */
    private void buildMySQLConnection() {
        getLogger().info("Building MySQL-Connection..");

        this.queryHandler = new QueryHandler(new JsonConfig(this.config.getObject().getJSONObject("mysql")).load(MySQLCredentials.class));
        //this.queryHandler.createBuilder("CREATE TABLE IF NOT EXISTS logs(numeric_id INT UNIQUE AUTO_INCREMENT, timestamp BIGINT, thread VARCHAR(64), guild_id BIGINT, text TEXT);").executeUpdateSync();
        //this.queryHandler.createBuilder("CREATE TABLE IF NOT EXISTS settings(numeric_id INT UNIQUE AUTO_INCREMENT, guild_id BIGINT, language VARCHAR(3), log_channel BIGINT, apply_channel BIGINT, verify_channel BIGINT, verify_webhook BIGINT, verify_webhook_url TEXT, verify_role BIGINT);").executeUpdateSync();

        getLogger().info("MySQL-Connection finished!");
    }
}

