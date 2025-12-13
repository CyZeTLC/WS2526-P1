package de.cyzetlc.hsbi.game.audio;

/**
 * The {@code Music} enumeration defines all background music tracks available in the game,
 * linking a logical name (e.g., MENU) to the physical resource path of the audio file.
 *
 * @see SoundManager
 *
 * @author Leonardo Parrino
 */
public enum Music {
    MENU("/assets/audio/background_music/Sonic Empire (Short Mix) (128kbit_AAC).m4a"),
    GAME("/assets/audio/background_music/Sonic Empire (Short Mix) (128kbit_AAC).m4a");

    /**
     * The resource path (classpath) to the corresponding audio file.
     */
    private final String path;

    /**
     * Constructs a {@code Music} enum constant with the specified resource path.
     *
     * @param path The classpath resource path of the audio file.
     */
    Music(String path) {
        this.path = path;
    }

    /**
     * Returns the resource path to the audio file associated with this music track.
     *
     * @return The resource path string.
     */
    public String path() {
        return path;
    }
}
