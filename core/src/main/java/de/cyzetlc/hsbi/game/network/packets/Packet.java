package de.cyzetlc.hsbi.game.network.packets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;

public abstract class Packet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1234567L;

    public static Logger logger = LoggerFactory.getLogger(Packet.class.getName());

    public abstract void handle();

    private boolean isUnique() {
        return false;
    }
}
