package de.cyzetlc.hsbi.game.audio;

public enum Sound {
    CLICK("/assets/audio/menu_click.wav"),
    ENEMY_DIE("/assets/audio/enemy_die.mp3"),
    PLAYER_DIE("/assets/audio/player_die.mp3"),
    JUMP("/assets/audio/jump.mp3");

    public final String path;

    Sound(String path) {
        this.path = path;
    }
}
