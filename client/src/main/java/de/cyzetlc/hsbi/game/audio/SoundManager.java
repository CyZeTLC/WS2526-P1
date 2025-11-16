package de.cyzetlc.hsbi.game.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static final Map<String, Media> mediaCache = new HashMap<>();
    private static double globalVolume = 1.0;

    @Getter
    private static boolean muted = false;

    private static MediaPlayer backgroundPlayer;

    /**
     * Spielt einen kurzen Soundeffekt ab. Unterstuetzt gleichzeitige Wiedergabe.
     */
    public static void play(Sound sound) {
        try {
            Media media = mediaCache.computeIfAbsent(sound.path, p ->
                    new Media(new File(p).toURI().toString())
            );

            MediaPlayer player = new MediaPlayer(media);
            applyVolume(player);
            player.setOnEndOfMedia(player::dispose); // Ressourcen freigeben
            player.play();
        } catch (Exception e) {
            System.err.println("Fehler beim Abspielen von Sound: " + sound.path);
            e.printStackTrace();
        }
    }

    /**
     * Spielt Musik im Hintergrund. Stoppt vorher laufende Musik automatisch.
     * @param music   Audiodatei
     * @param looping true, wenn die Musik unendlich wiederholt werden soll
     */
    public static void playBackground(Music music, boolean looping) {
        stopBackground();
        try {
            Media media = mediaCache.computeIfAbsent(music.path(), p ->
                    new Media(new File(p).toURI().toString())
            );

            backgroundPlayer = new MediaPlayer(media);
            backgroundPlayer.setCycleCount(looping ? MediaPlayer.INDEFINITE : 1);
            applyVolume(backgroundPlayer);
            backgroundPlayer.setOnEndOfMedia(() -> {
                if (!looping) {
                    stopBackground();
                }
            });
            backgroundPlayer.play();
        } catch (Exception e) {
            System.err.println("Fehler beim Abspielen von Hintergrundmusik: " + music.path());
            e.printStackTrace();
        }
    }

    /** Stoppt die laufende Hintergrundmusik (falls vorhanden). */
    public static void stopBackground() {
        if (backgroundPlayer != null) {
            try {
                backgroundPlayer.stop();
                backgroundPlayer.dispose();
            } catch (Exception ignored) {
                // nichts weiter tun, wir wollen nur sicher freigeben
            }
            backgroundPlayer = null;
        }
    }

    /** Setzt die globale Lautstaerke (0.0 - 1.0) */
    public static void setVolume(double volume) {
        globalVolume = Math.max(0, Math.min(1, volume));
        applyVolume(backgroundPlayer);
    }

    /** Gibt die aktuelle Lautstaerke zurueck */
    public static double getVolume() {
        return globalVolume;
    }

    /** Aktiviert oder deaktiviert Stummschaltung. */
    public static void setMuted(boolean muted) {
        SoundManager.muted = muted;
        applyVolume(backgroundPlayer);
    }

    private static void applyVolume(MediaPlayer player) {
        if (player != null) {
            player.setVolume(muted ? 0.0 : globalVolume);
        }
    }

    /** Entfernt alle gecachten Media-Objekte */
    public static void clearCache() {
        mediaCache.clear();
    }
}
