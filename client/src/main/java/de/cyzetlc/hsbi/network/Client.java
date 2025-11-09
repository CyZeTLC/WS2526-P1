package de.cyzetlc.hsbi.network;

import de.cyzetlc.hsbi.game.event.EventCancelable;
import de.cyzetlc.hsbi.game.event.EventManager;
import de.cyzetlc.hsbi.game.event.impl.ReceivePacketEvent;
import de.cyzetlc.hsbi.game.listener.PacketListener;
import de.cyzetlc.hsbi.game.listener.UserMessageListener;
import de.cyzetlc.hsbi.game.network.packets.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    @Getter
    private static final Logger logger = LoggerFactory.getLogger(Client.class.getName());

    @Getter
    private static ClientPlayer thePlayer;

    private static ExecutorService ex;

    /**
     * The main function establishes a client socket connection to a server, sends and receives messages, and closes the
     * connection when the user enters "Exit".
     */
    public static void main(String[] args) throws IOException  {
        thePlayer = new ClientPlayer();
        ex = Executors.newCachedThreadPool();
        //ex.execute(() -> Window.getInstance().run());

        try {
            Scanner sc = new Scanner(System.in);
            InetAddress ip = InetAddress.getByName("localhost");
            Socket s = new Socket(ip,25570);

            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            byte[] bytes = SerializationUtils.serialize(new ClientLoginPacket(thePlayer.getUuid()));
            dos.write(bytes);

            while(true) {
                byte[] received = new byte[500000]; // max: 0.5 MB

                // receiving packet
                int bytesRead = dis.read(received);
                byte[] actualReceivedData = new byte[bytesRead];
                System.arraycopy(received, 0, actualReceivedData, 0, bytesRead);
                Packet packet = SerializationUtils.deserialize(actualReceivedData, Packet.class);

                if (((EventCancelable)new ReceivePacketEvent(packet, s).call()).isCancelled()) {
                    break;
                }

                /*
                if (packet instanceof ClientKickPacket) {
                    break;
                } else if (packet instanceof UserMessagePacket msg) {
                    logger.info(msg.getMessage());
                }*/

                dos.write(SerializationUtils.serialize(new ClientPlayer()));

                Thread.sleep(50); // 1 Tick
            }
            sc.close();
            dis.close();
            dos.close();
        }
        catch(Exception e) {
            logger.error(e.getMessage());
        }
    }
}
