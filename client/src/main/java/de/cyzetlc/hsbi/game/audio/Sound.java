package de.cyzetlc.hsbi.game.audio;

public enum Sound {
    CLICK("/assets/audio/menu_click.wav"),
    ENEMY_DIE("/assets/audio/enemy_die.mp3"),
    PLAYER_DIE("/assets/audio/player_die.mp3"),
    JUMP("/assets/audio/jump.mp3"),
    JUMP_BOOST("/assets/audio/SoundEvents/Jump-Boost-Soundwav.wav"),
    SPEED_BUFF("/assets/audio/SoundEvents/Speed-Buff-Sound.wav"),
    USB_STICK("/assets/audio/SoundEvents/USB-StickSound.wav");

    public final String path;

    Sound(String path) {
        this.path = path;
    }
}
