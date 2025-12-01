package de.cyzetlc.hsbi.game.network.packets;

import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.world.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class ClientDataPacket extends Packet {
    @Getter @Setter
    private Location location;

    public ClientDataPacket(Location location) {
        this.location = location;
    }

    @Override
    public void handle() {

    }
}
