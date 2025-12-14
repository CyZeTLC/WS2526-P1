package de.cyzetlc.hsbi.game.audio;

/**
 * Die {@code Music} Enumeration definiert alle im Spiel verfügbaren Hintergrundmusiktitel
 * und verknüpft einen logischen Namen (z. B. MENU) mit dem physischen Ressourcenpfad
 * der Audiodatei.
 *
 * @see SoundManager
 *
 * @author Tom Coombs
 */
public enum Music {
    MENU("/assets/audio/background_music/Sonic Empire (Short Mix) (128kbit_AAC).m4a"),
    GAME("/assets/audio/background_music/Sonic Empire (Short Mix) (128kbit_AAC).m4a");

    /**
     * Der Ressourcenpfad (Classpath) zur entsprechenden Audiodatei.
     */
    private final String path;

    /**
     * Konstruiert eine {@code Music} Enum-Konstante mit dem angegebenen Ressourcenpfad.
     *
     * @param path Der Classpath-Ressourcenpfad der Audiodatei.
     */
    Music(String path) {
        this.path = path;
    }

    /**
     * Gibt den Ressourcenpfad zur Audiodatei zurück, die diesem Musiktitel zugeordnet ist.
     *
     * @return Der Ressourcenpfad als String.
     */
    public String path() {
        return path;
    }
}