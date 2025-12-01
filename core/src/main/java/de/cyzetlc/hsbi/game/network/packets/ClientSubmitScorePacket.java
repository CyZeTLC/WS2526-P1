package de.cyzetlc.hsbi.game.network.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ClientSubmitScorePacket extends Packet { // Muss 'Packet' erweitern und serialisierbar sein
    private final UUID playerId;
    private final long finalScore;
    private final String levelId;

    @Override
    public void handle() {

    }
}
