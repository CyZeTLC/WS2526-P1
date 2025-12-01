package de.cyzetlc.hsbi.game.network.packets;

import de.cyzetlc.hsbi.game.world.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class ClientDataPacket extends Packet {
    private Location location;

    private UUID uuid;

    private String name;

    public ClientDataPacket(UUID uuid, String name, Location location) {
        this.location = location;
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public void handle() {

    }
}
