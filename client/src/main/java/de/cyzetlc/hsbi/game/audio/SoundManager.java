package de.cyzetlc.hsbi.game.audio;

import de.cyzetlc.hsbi.game.Game;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SoundManager {

    private static final Map<String, Media> mediaCache = new HashMap<>();
    private static double globalVolume = 1.0;
    private static final Object duckLock = new Object();
    private static int duckDepth = 0;

    @Getter
    private static boolean muted = false;

    private static MediaPlayer backgroundPlayer;

    /**
     * Spielt einen kurzen Soundeffekt ab. Unterstuetzt gleichzeitige Wiedergabe.
     */
    public static void play(Sound sound) {
        try {
            Media media = mediaCache.computeIfAbsent(sound.path, SoundManager::loadMedia);

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
     * Spielt einen kurzen Soundeffekt mit expliziter Lautstaerke (0.0 - 1.0).
     * Respektiert mute und begrenzt auf die aktuell gesetzte globale Lautstaerke.
     */
    public static void play(Sound sound, double volumeOverride) {
        try {
            Media media = mediaCache.computeIfAbsent(sound.path, SoundManager::loadMedia);

            MediaPlayer player = new MediaPlayer(media);
            double effectiveVolume = muted ? 0.0 : Math.min(Math.max(0.0, volumeOverride), globalVolume);
            player.setVolume(effectiveVolume);
            player.setOnEndOfMedia(player::dispose); // Ressourcen freigeben
            player.play();
        } catch (Exception e) {
            System.err.println("Fehler beim Abspielen von Sound: " + sound.path);
            e.printStackTrace();
        }
    }

    /**
     * Spielt einen Soundeffekt und duckt gleichzeitig die Hintergrundmusik auf einen Zielwert.
     *
     * @param sound          zu spielender Effekt
     * @param volumeOverride Lautstaerke des Effekts (0.0 - 1.0)
     * @param duckVolume     Lautstaerke fuer die Hintergrundmusik waehrend des Effekts (0.0 - 1.0)
     */
    public static void playWithDuck(Sound sound, double volumeOverride, double duckVolume) {
        try {
            Media media = mediaCache.computeIfAbsent(sound.path, SoundManager::loadMedia);
            MediaPlayer player = new MediaPlayer(media);

            double effectiveVolume = muted ? 0.0 : Math.min(Math.max(0.0, volumeOverride), 1.0);
            player.setVolume(effectiveVolume);

            duckBackground(duckVolume);

            player.setOnEndOfMedia(() -> {
                player.dispose();
                unduckBackground();
            });
            player.setOnError(SoundManager::unduckBackground);
            player.play();
        } catch (Exception e) {
            System.err.println("Fehler beim Abspielen von Sound: " + sound.path);
            e.printStackTrace();
            unduckBackground();
        }
    }

    /**
     * Spielt Musik im Hintergrund. Stoppt vorher laufende Musik automatisch.
     * @param music   Audiodatei
     * @param looping true, wenn die Musik unendlich wiederholt werden soll
     */
    public static void playBackground(Music music, boolean looping) {
        CompletableFuture.runAsync(() -> {
            stopBackground();
            try {
                Media media = mediaCache.computeIfAbsent(music.path(), SoundManager::loadMedia);

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
        });
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

    /** Laedt alle Sounds und Musiktitel einmalig in den Cache (async). */
    public static void preloadAll() {
        CompletableFuture.runAsync(() -> {
            for (Sound sound : Sound.values()) {
                mediaCache.computeIfAbsent(sound.path, SoundManager::loadMedia);
            }
            for (Music music : Music.values()) {
                mediaCache.computeIfAbsent(music.path(), SoundManager::loadMedia);
            }
        });
    }

    /** Setzt die globale Lautstaerke (0.0 - 1.0) */
    public static void setVolume(double volume) {
        globalVolume = Math.max(0, Math.min(1, volume));
        Game.getInstance().getConfig().getObject().put("soundVolume", volume);
        Game.getInstance().getConfig().save();
        applyVolume(backgroundPlayer);
    }

    /** Gibt die aktuelle Lautstaerke zurueck */
    public static double getVolume() {
        return globalVolume;
    }

    /** Aktiviert oder deaktiviert Stummschaltung. */
    public static void setMuted(boolean muted) {
        SoundManager.muted = muted;
        Game.getInstance().getConfig().getObject().put("soundMuted", muted);
        Game.getInstance().getConfig().save();
        applyVolume(backgroundPlayer);
    }

    private static void applyVolume(MediaPlayer player) {
        if (player != null) {
            player.setVolume(muted ? 0.0 : globalVolume);
        }
    }

    private static void duckBackground(double duckVolume) {
        synchronized (duckLock) {
            duckDepth++;
            if (backgroundPlayer != null) {
                double target = Math.min(Math.max(0.0, duckVolume), globalVolume);
                backgroundPlayer.setVolume(muted ? 0.0 : target);
            }
        }
    }

    private static void unduckBackground() {
        synchronized (duckLock) {
            if (duckDepth > 0) {
                duckDepth--;
            }
            if (duckDepth == 0) {
                applyVolume(backgroundPlayer);
            }
        }
    }

    private static Media loadMedia(String path) {
        try {
            // Versuche zuerst, die Datei als Resource vom Classpath zu laden
            String resourcePath = path.startsWith("/") ? path : "/" + path;
            URL resource = SoundManager.class.getResource(resourcePath);
            if (resource != null) {
                return new Media(resource.toExternalForm());
            }

            // Fallback: direkte Datei auf dem Dateisystem
            File file = new File(path);
            if (file.exists()) {
                return new Media(file.toURI().toString());
            }

            throw new IllegalArgumentException("Media nicht gefunden: " + path);
        } catch (Exception e) {
            System.err.println("Fehler beim Laden von Media: " + path);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /** Entfernt alle gecachten Media-Objekte */
    public static void clearCache() {
        mediaCache.clear();
    }
}
