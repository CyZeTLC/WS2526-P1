package de.cyzetlc.hsbi.network;

import de.cyzetlc.hsbi.game.entity.Player;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
public class ClientPlayer implements Serializable {
    @Serial
    private static final long serialVersionUID = 2234567L;

    private final UUID uuid;

    private final Player player;

    public ClientPlayer(Player player) {
        this.uuid = player.getUuid();
        this.player = player;
    }
}
