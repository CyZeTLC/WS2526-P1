package de.cyzetlc.hsbi.game.audio;

public enum Music {
    MENU("assets/audio/background_music/Sonic Empire (Short Mix) (128kbit_AAC).m4a"),
    GAME("assets/audio/background_music/Sonic Empire (Short Mix) (128kbit_AAC).m4a");

    private final String path;

    Music(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
