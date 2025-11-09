package de.cyzetlc.hsbi.game.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static final Map<String, Media> mediaCache = new HashMap<>();
    private static double globalVolume = 1.0;
    /**
     * -- GETTER --
     * Gibt zurück, ob Mute aktiv ist
     * -- SETTER --
     * Aktiviert oder deaktiviert Mute

     */
    @Setter
    @Getter
    private static boolean muted = false;

    /**
     * Spielt einen Sound ab. Unterstützt gleichzeitige Wiedergabe.
     * @param sound Pfad zur Audiodatei (z. B. "sounds/click.mp3")
     */
    public static void play(Sound sound) {
        try {
            Media media = mediaCache.computeIfAbsent(sound.path, p ->
                    new Media(new File(p).toURI().toString())
            );

            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(muted ? 0.0 : globalVolume);
            player.setOnEndOfMedia(player::dispose); // Ressourcen freigeben
            player.play();
        } catch (Exception e) {
            System.err.println("Fehler beim Abspielen von Sound: " + sound.path);
            e.printStackTrace();
        }
    }

    /** Setzt die globale Lautstärke (0.0–1.0) */
    public static void setVolume(double volume) {
        globalVolume = Math.max(0, Math.min(1, volume));
    }

    /** Gibt die aktuelle Lautstärke zurück */
    public static double getVolume() {
        return globalVolume;
    }

    /** Entfernt alle gecachten Media-Objekte */
    public static void clearCache() {
        mediaCache.clear();
    }
}
