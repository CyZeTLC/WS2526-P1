package de.cyzetlc.hsbi.network;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.event.impl.ReceivePacketEvent;
import de.cyzetlc.hsbi.game.network.packets.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    @Getter
    private static final Logger logger = LoggerFactory.getLogger(Client.class.getName());

    @Getter
    private static ClientPlayer thePlayer;

    // Netzwerk-Instanzattribute
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private final ExecutorService networkExecutor;

    public Client() {
        thePlayer = new ClientPlayer(Game.thePlayer);
        // Verwende einen dedizierten Thread Pool für Netzwerk-Aufgaben
        this.networkExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Stellt die Verbindung her und startet den Empfangs-Thread.
     */
    public void connectAndRun() {
        try {
            logger.info("Connecting to server...");
            InetAddress ip = InetAddress.getByName("localhost");
            this.socket = new Socket(ip, 25570);

            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            logger.info("Connection successful.");

            // Initiales Login-Packet senden
            sendPacket(new ClientLoginPacket(thePlayer.getUuid()));

            networkExecutor.execute(new ReceiverTask());
            networkExecutor.execute(new SenderTask());

            // Window.getInstance().run();

        } catch (Exception e) {
            logger.error("Client connection failed: " + e.getMessage());
            closeConnection();
        }
    }

    /**
     * Runnable für den dedizierten Empfangs-Thread.
     * Erwartet und deserialisiert einkommende Pakete.
     */
    private class ReceiverTask implements Runnable {
        @Override
        public void run() {
            byte[] received = new byte[500000]; // max: 0.5 MB
            while (socket != null && !socket.isClosed()) {
                try {
                    // Blockierende Operation: Wartet auf Daten
                    int bytesRead = dis.read(received);

                    if (bytesRead <= 0) {
                        // Verbindung geschlossen oder unerwartet beendet
                        break;
                    }

                    byte[] actualReceivedData = new byte[bytesRead];
                    System.arraycopy(received, 0, actualReceivedData, 0, bytesRead);

                    Packet packet = SerializationUtils.deserialize(actualReceivedData, Packet.class);

                    // Paket über das Event-System verteilen
                    if (((EventCancelable) new ReceivePacketEvent(packet, socket).call()).isCancelled()) {
                        break;
                    }

                } catch (IOException e) {
                    logger.error("Connection lost from server: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    logger.error("Error during packet processing: " + e.getMessage());
                }
            }
            closeConnection();
        }
    }

    /**
     * Runnable für den dedizierten Sende-Thread (z.b. Position Update).
     * Schickt regelmäßig den Spielerstatus.
     */
    private class SenderTask implements Runnable {
        @Override
        public void run() {
            while (socket != null && !socket.isClosed()) {
                try {
                    // Sende alle 50ms (20 Ticks/Sekunde) ein Status-Update
                    sendPacket(new ClientDataPacket(thePlayer.getUuid(), thePlayer.getPlayer().getDisplayName(), thePlayer.getPlayer().getLocation()));

                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Thread wurde unterbrochen
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException e) {
                    logger.error("Error sending heartbeats: " + e.getMessage());
                    break;
                }
            }
        }
    }

    /**
     * Serialisiert und sendet ein beliebiges Paket sicher über den DataOutputStream.
     * Synchronisiert, um Race Conditions zu vermeiden, falls mehrere Threads gleichzeitig senden.
     */
    public synchronized void sendPacket(Packet packet) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Cannot send packet: Socket is closed.");
        }

        byte[] bytes = SerializationUtils.serialize(packet);
        this.dos.write(bytes);
        this.dos.flush();
    }

    /**
     * Logik zum Senden des finalen Highscores an den Server.
     */
    public void sendFinalScore(long score) {
        try {
            ClientSubmitScorePacket scorePacket = new ClientSubmitScorePacket(
                    thePlayer.getUuid(),
                    score,
                    "Level_1"
            );

            sendPacket(scorePacket);
            logger.info("Sent final score: " + score);

        } catch (IOException e) {
            logger.error("Failed to send score packet: " + e.getMessage());
        }
    }

    /**
     * Schließt alle Ressourcen.
     */
    public void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            networkExecutor.shutdownNow();
        } catch (IOException e) {
            logger.error("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        // Starte die Verbindung und die Netzwerk-Threads
        client.connectAndRun();

        try {
            Thread.sleep(5000); // 5 Sekunden warten
            client.sendFinalScore(12345L);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Das Programm muss manuell beendet werden, oder du implementierst
        // eine Methode zum kontrollierten Beenden über die Scanner-Eingabe.
    }
}