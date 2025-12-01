package de.cyzetlc.hsbi.game.network.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class ServerSendHighscoresPacket extends Packet {
    // Eine innere, serialisierbare Klasse, um die Daten zu speichern
    public static class HighscoreEntry implements java.io.Serializable {
        public String playerName;
        public long score;
    }

    private final List<HighscoreEntry> topScores;

    @Override
    public void handle() {

    }
}
