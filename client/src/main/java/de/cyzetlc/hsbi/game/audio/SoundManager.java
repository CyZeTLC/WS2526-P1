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
/**
 * The {@code SoundManager} utility class provides static methods for managing all audio playback
 * in the game, including sound effects and background music.
 * <p>
 * It implements functionality for media caching, volume control (including global mute),
 * persistence via configuration, and advanced features like "audio ducking" to lower
 * background music volume temporarily when a key sound effect plays.
 *
 * @see Sound
 * @see Music
 * @see Game#getConfig()
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class SoundManager {

    /**
     * Cache storing pre-loaded {@code Media} objects mapped by their file path to avoid
     * repeated loading times and resource spikes.
     */
    private static final Map<String, Media> mediaCache = new HashMap<>();

    /**
     * The master volume level (0.0 to 1.0) applied to all sound playback.
     */
    private static double globalVolume = 1.0;

    /**
     * Decibel (dB) increase applied to sounds to make them slightly louder than the default 1.0.
     */
    private static final double DB_BOOST = 10.0;

    /**
     * The calculated linear factor corresponding to the {@code DB_BOOST} (approximately 3.162).
     */
    private static final double DB_FACTOR = Math.pow(10.0, DB_BOOST / 20.0); // ~3.162

    /**
     * Lock object used to synchronize access to {@code duckDepth} during audio ducking operations.
     */
    private static final Object duckLock = new Object();

    /**
     * Counter for active audio ducking requests. Used to restore background volume only when
     * all temporary sounds have finished playing.
     */
    private static int duckDepth = 0;

    /**
     * Flag indicating whether all sound output is currently muted.
     */
    @Getter
    private static boolean muted = false;

    /**
     * The dedicated media player instance for background music playback.
     */
    private static MediaPlayer backgroundPlayer;

    /**
     * Plays a short sound effect once. Supports concurrent playback of multiple effects.
     * The sound's volume is limited by the current {@code globalVolume} and respects the {@code muted} state.
     *
     * @param sound The {@code Sound} enum entry to play.
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
     * Plays a short sound effect with an explicit volume override.
     * <p>
     * The effective volume is capped by the current {@code globalVolume} setting and respects the {@code muted} state.
     *
     * @param sound The {@code Sound} enum entry to play.
     * @param volumeOverride The desired base volume (0.0 - 1.0).
     */
    public static void play(Sound sound, double volumeOverride) {
        try {
            Media media = mediaCache.computeIfAbsent(sound.path, SoundManager::loadMedia);

            MediaPlayer player = new MediaPlayer(media);
            double base = clamp01(volumeOverride);
            double limited = Math.min(base, globalVolume);
            double effectiveVolume = muted ? 0.0 : boostedVolume(limited);
            player.setVolume(effectiveVolume);
            player.setOnEndOfMedia(player::dispose); // Ressourcen freigeben
            player.play();
        } catch (Exception e) {
            System.err.println("Fehler beim Abspielen von Sound: " + sound.path);
            e.printStackTrace();
        }
    }

    /**
     * Plays a short sound effect and simultaneously lowers ("ducks") the background music volume
     * to a specified target value for the duration of the sound effect.
     * <p>
     * Ducking is managed via {@code duckDepth} to handle overlapping sounds correctly.
     *
     * @param sound The sound effect to play.
     * @param volumeOverride The volume of the effect (0.0 - 1.0).
     * @param duckVolume The target volume for the background music while the effect plays (0.0 - 1.0).
     */
    public static void playWithDuck(Sound sound, double volumeOverride, double duckVolume) {
        try {
            Media media = mediaCache.computeIfAbsent(sound.path, SoundManager::loadMedia);
            MediaPlayer player = new MediaPlayer(media);

            double base = clamp01(volumeOverride);
            double effectiveVolume = muted ? 0.0 : boostedVolume(base);
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
     * Stops any currently playing background music and starts playback of the specified music track.
     * <p>
     * This operation runs asynchronously to prevent blocking the JavaFX Application Thread.
     *
     * @param music The {@code Music} enum entry to play.
     * @param looping {@code true} if the music should repeat indefinitely; {@code false} otherwise.
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

    /**
     * Stops the currently running background music player and releases its resources.
     */
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

    /**
     * Preloads all defined {@code Sound} and {@code Music} tracks into the media cache asynchronously.
     * <p>
     * This is typically called once at startup (e.g., in the loading screen) to eliminate
     * audio delays during gameplay.
     */
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

    /**
     * Sets the global master volume level (0.0 - 1.0).
     * <p>
     * The new volume is clamped, saved to the game configuration, and immediately applied
     * to the currently playing background music.
     *
     * @param volume The new volume level (0.0 to 1.0).
     */
    public static void setVolume(double volume) {
        globalVolume = Math.max(0, Math.min(1, volume));
        Game.getInstance().getConfig().getObject().put("soundVolume", volume);
        Game.getInstance().getConfig().save();
        applyVolume(backgroundPlayer);
    }

    /**
     * Returns the current global master volume level.
     *
     * @return The current volume (0.0 to 1.0).
     */
    public static double getVolume() {
        return globalVolume;
    }

    /**
     * Activates or deactivates the global mute state.
     * <p>
     * The mute state is saved to the game configuration, and the new volume (0.0 or the current {@code globalVolume})
     * is immediately applied to the background player.
     *
     * @param muted {@code true} to mute all sound; {@code false} to unmute.
     */
    public static void setMuted(boolean muted) {
        SoundManager.muted = muted;
        Game.getInstance().getConfig().getObject().put("soundMuted", muted);
        Game.getInstance().getConfig().save();
        applyVolume(backgroundPlayer);
    }

    /**
     * Applies the current effective volume (respecting global volume, boost, and mute state)
     * to a given {@code MediaPlayer} instance.
     *
     * @param player The player to apply the volume setting to.
     */
    private static void applyVolume(MediaPlayer player) {
        if (player != null) {
            player.setVolume(muted ? 0.0 : boostedVolume(globalVolume));
        }
    }

    /**
     * Temporarily lowers the background music volume to the specified {@code duckVolume}.
     * <p>
     * Increases the {@code duckDepth} counter and synchronizes access via {@code duckLock}.
     *
     * @param duckVolume The temporary target base volume (0.0 - 1.0).
     */
    private static void duckBackground(double duckVolume) {
        synchronized (duckLock) {
            duckDepth++;
            if (backgroundPlayer != null) {
                double targetBase = Math.max(duckVolume, globalVolume);
                backgroundPlayer.setVolume(muted ? 0.0 : boostedVolume(targetBase));
            }
        }
    }

    /**
     * Restores the background music volume to the normal {@code globalVolume} if no other
     * temporary sounds are currently active (i.e., {@code duckDepth} returns to 0).
     * <p>
     * Decreases the {@code duckDepth} counter and synchronizes access via {@code duckLock}.
     */
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

    /**
     * Attempts to load an audio file into a {@code Media} object.
     * <p>
     * It first tries to load the file as a resource from the application's classpath (for packed JARs).
     * If that fails, it falls back to loading the file directly from the filesystem path.
     *
     * @param path The resource or file path of the audio file.
     * @return A loaded {@code Media} object.
     * @throws RuntimeException if the media file cannot be found or loaded.
     */
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

            Game.getLogger().error("Media nicht gefunden: {}", path);
            return null;
        } catch (Exception e) {
            System.err.println("Fehler beim Laden von Media: " + path);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the "boosted" volume level by multiplying the base volume by the {@code DB_FACTOR}.
     * <p>
     * The result is clamped between 0.0 and 1.0. This is used to make sound effects audibly clearer.
     *
     * @param baseVolume The input volume (0.0 to 1.0).
     * @return The boosted and clamped volume.
     */
    private static double boostedVolume(double baseVolume) {
        double boosted = clamp01(baseVolume) * DB_FACTOR;
        return clamp01(boosted);
    }

    /**
     * Clamps a double value between 0.0 and 1.0.
     *
     * @param value The input value.
     * @return The value constrained to the range [0.0, 1.0].
     */
    private static double clamp01(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }
}
