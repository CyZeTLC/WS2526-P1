package de.cyzetlc.hsbi.game.audio;

/**
 * The {@code Sound} enumeration defines all short sound effects (SFX) available in the game,
 * linking a logical name (e.g., CLICK) to the physical resource path of the audio file.
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
     * The resource path (classpath) to the corresponding audio file.
     */
    public final String path;

    /**
     * Constructs a {@code Sound} enum constant with the specified resource path.
     *
     * @param path The classpath resource path of the audio file.
     */
    Sound(String path) {
        this.path = path;
    }
}
