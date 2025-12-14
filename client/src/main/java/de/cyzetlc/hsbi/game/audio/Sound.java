package de.cyzetlc.hsbi.game.audio;

/**
 * Die {@code Sound} Enumeration definiert alle kurzen Soundeffekte (SFX), die im Spiel
 * verfügbar sind, und verknüpft einen logischen Namen (z. B. CLICK) mit dem physischen
 * Ressourcenpfad der Audiodatei.
 *
 * @see SoundManager
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public enum Sound {
    CLICK("/assets/audio/menu_click.wav"),
    ENEMY_DIE("/assets/audio/enemy_die.mp3"),
    PLAYER_DIE("/assets/audio/player_die.mp3"),
    JUMP("/assets/audio/jump.mp3"),
    JUMP_BOOST("/assets/audio/SoundEvents/Jump-Boost-Soundwav.wav"),
    SPEED_BUFF("/assets/audio/SoundEvents/Speed-Buff-Sound.wav"),
    USB_STICK("/assets/audio/SoundEvents/USB-StickSound.wav"),
    ZIEL_ERREICHT("/assets/audio/SoundEvents/Ziel-erreicht-Sound.wav");

    /**
     * Der Ressourcenpfad (Classpath) zur entsprechenden Audiodatei.
     */
    public final String path;

    /**
     * Konstruiert eine {@code Sound} Enum-Konstante mit dem angegebenen Ressourcenpfad.
     *
     * @param path Der Classpath-Ressourcenpfad der Audiodatei.
     */
    Sound(String path) {
        this.path = path;
    }
}