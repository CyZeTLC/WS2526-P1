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
 * Die {@code SoundManager} Utility-Klasse stellt statische Methoden zur Verwaltung
 * der gesamten Audio-Wiedergabe im Spiel bereit, einschließlich Soundeffekten
 * und Hintergrundmusik.
 * <p>
 * Sie implementiert Funktionalität für Medien-Caching, Lautstärkeregelung
 * (einschließlich globaler Stummschaltung), Persistenz über die Konfiguration
 * und erweiterte Funktionen wie "Audio Ducking", um die Lautstärke der
 * Hintergrundmusik vorübergehend zu senken, wenn ein wichtiger Soundeffekt
 * abgespielt wird.
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
     * Cache, der vorgeladene {@code Media}-Objekte speichert, die ihrem Dateipfad
     * zugeordnet sind, um wiederholte Ladezeiten und Ressourcen-Spitzen zu vermeiden.
     */
    private static final Map<String, Media> mediaCache = new HashMap<>();

    /**
     * Der globale Master-Lautstärkepegel (0.0 bis 1.0), der auf die gesamte Soundwiedergabe angewendet wird.
     */
    private static double globalVolume = 1.0;

    /**
     * Dezibel (dB) Erhöhung, die auf Sounds angewendet wird, um sie etwas lauter als den Standardwert 1.0 zu machen.
     */
    private static final double DB_BOOST = 10.0;

    /**
     * Der berechnete lineare Faktor, der dem {@code DB_BOOST} entspricht (ungefähr 3.162).
     */
    private static final double DB_FACTOR = Math.pow(10.0, DB_BOOST / 20.0); // ~3.162

    /**
     * Sperrobjekt (Lock) zur Synchronisierung des Zugriffs auf {@code duckDepth} während Audio-Ducking-Operationen.
     */
    private static final Object duckLock = new Object();

    /**
     * Zähler für aktive Audio-Ducking-Anfragen. Wird verwendet, um die Hintergrundlautstärke
     * erst dann wiederherzustellen, wenn alle temporären Sounds beendet wurden.
     */
    private static int duckDepth = 0;

    /**
     * Flag, das angibt, ob die gesamte Soundausgabe derzeit stummgeschaltet ist.
     */
    @Getter
    private static boolean muted = false;

    /**
     * Die dedizierte MediaPlayer-Instanz für die Wiedergabe der Hintergrundmusik.
     */
    private static MediaPlayer backgroundPlayer;

    /**
     * Spielt einen kurzen Soundeffekt einmal ab. Unterstützt die gleichzeitige Wiedergabe mehrerer Effekte.
     * Die Lautstärke des Sounds wird durch die aktuelle {@code globalVolume} begrenzt und
     * berücksichtigt den {@code muted}-Zustand.
     *
     * @param sound Der abzuspielende {@code Sound} Enum-Eintrag.
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
     * Spielt einen kurzen Soundeffekt mit einer expliziten Lautstärke-Überschreibung ab.
     * <p>
     * Die effektive Lautstärke wird durch die aktuelle {@code globalVolume}-Einstellung begrenzt
     * und berücksichtigt den {@code muted}-Zustand.
     *
     * @param sound Der abzuspielende {@code Sound} Enum-Eintrag.
     * @param volumeOverride Die gewünschte Basis-Lautstärke (0.0 - 1.0).
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
     * Spielt einen kurzen Soundeffekt ab und senkt gleichzeitig ("ducks") die Lautstärke der
     * Hintergrundmusik auf einen bestimmten Zielwert für die Dauer des Soundeffekts.
     * <p>
     * Das Ducking wird über {@code duckDepth} verwaltet, um überlappende Sounds korrekt zu behandeln.
     *
     * @param sound Der abzuspielende Soundeffekt.
     * @param volumeOverride Die Lautstärke des Effekts (0.0 - 1.0).
     * @param duckVolume Die Ziel-Lautstärke für die Hintergrundmusik, während der Effekt abgespielt wird (0.0 - 1.0).
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
     * Stoppt die aktuell laufende Hintergrundmusik und startet die Wiedergabe des angegebenen Musiktitels.
     * <p>
     * Dieser Vorgang wird asynchron ausgeführt, um eine Blockierung des JavaFX Application Thread zu verhindern.
     *
     * @param music Der abzuspielende {@code Music} Enum-Eintrag.
     * @param looping {@code true}, wenn die Musik unbegrenzt wiederholt werden soll; {@code false} andernfalls.
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
     * Stoppt den aktuell laufenden Hintergrundmusik-Player und gibt dessen Ressourcen frei.
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
     * Lädt alle definierten {@code Sound}- und {@code Music}-Titel asynchron in den Medien-Cache vor.
     * <p>
     * Dies wird typischerweise einmal beim Start aufgerufen (z. B. im Ladebildschirm), um
     * Audioverzögerungen während des Gameplays zu eliminieren.
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
     * Stellt den globalen Master-Lautstärkepegel ein (0.0 - 1.0).
     * <p>
     * Die neue Lautstärke wird begrenzt (clamped), in der Spielkonfiguration gespeichert
     * und sofort auf die aktuell spielende Hintergrundmusik angewendet.
     *
     * @param volume Der neue Lautstärkepegel (0.0 bis 1.0).
     */
    public static void setVolume(double volume) {
        globalVolume = Math.max(0, Math.min(1, volume));
        Game.getInstance().getConfig().getObject().put("soundVolume", volume);
        Game.getInstance().getConfig().save();
        applyVolume(backgroundPlayer);
    }

    /**
     * Gibt den aktuellen globalen Master-Lautstärkepegel zurück.
     *
     * @return Die aktuelle Lautstärke (0.0 bis 1.0).
     */
    public static double getVolume() {
        return globalVolume;
    }

    /**
     * Aktiviert oder deaktiviert den globalen Stummschaltzustand.
     * <p>
     * Der Stummschaltzustand wird in der Spielkonfiguration gespeichert, und die neue Lautstärke
     * (0.0 oder die aktuelle {@code globalVolume}) wird sofort auf den Hintergrund-Player angewendet.
     *
     * @param muted {@code true}, um den gesamten Sound stummzuschalten; {@code false}, um die Stummschaltung aufzuheben.
     */
    public static void setMuted(boolean muted) {
        SoundManager.muted = muted;
        Game.getInstance().getConfig().getObject().put("soundMuted", muted);
        Game.getInstance().getConfig().save();
        applyVolume(backgroundPlayer);
    }

    /**
     * Wendet die aktuell effektive Lautstärke (unter Berücksichtigung von globaler Lautstärke,
     * Boost und Stummschaltzustand) auf eine gegebene {@code MediaPlayer}-Instanz an.
     *
     * @param player Der Player, auf den die Lautstärkeeinstellung angewendet werden soll.
     */
    private static void applyVolume(MediaPlayer player) {
        if (player != null) {
            player.setVolume(muted ? 0.0 : boostedVolume(globalVolume));
        }
    }

    /**
     * Senkt die Lautstärke der Hintergrundmusik vorübergehend auf die angegebene {@code duckVolume}.
     * <p>
     * Erhöht den {@code duckDepth}-Zähler und synchronisiert den Zugriff über {@code duckLock}.
     *
     * @param duckVolume Die temporäre Ziel-Basis-Lautstärke (0.0 - 1.0).
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
     * Stellt die Lautstärke der Hintergrundmusik auf die normale {@code globalVolume} wieder her,
     * wenn keine anderen temporären Sounds mehr aktiv sind (d. h. {@code duckDepth} kehrt zu 0 zurück).
     * <p>
     * Verringert den {@code duckDepth}-Zähler und synchronisiert den Zugriff über {@code duckLock}.
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
     * Versucht, eine Audiodatei in ein {@code Media}-Objekt zu laden.
     * <p>
     * Zuerst wird versucht, die Datei als Ressource aus dem Classpath der Anwendung zu laden
     * (für gepackte JARs). Schlägt dies fehl, wird als Fallback die Datei direkt über den
     * Dateisystempfad geladen.
     *
     * @param path Der Ressourcen- oder Dateipfad der Audiodatei.
     * @return Ein geladenes {@code Media}-Objekt.
     * @throws RuntimeException wenn die Mediendatei nicht gefunden oder geladen werden kann.
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
     * Berechnet den "verstärkten" Lautstärkepegel, indem die Basis-Lautstärke mit dem {@code DB_FACTOR}
     * multipliziert wird.
     * <p>
     * Das Ergebnis wird zwischen 0.0 und 1.0 begrenzt (clamped). Dies wird verwendet, um Soundeffekte
     * hörbar klarer zu machen.
     *
     * @param baseVolume Die Eingabelautstärke (0.0 bis 1.0).
     * @return Die verstärkte und begrenzte Lautstärke.
     */
    private static double boostedVolume(double baseVolume) {
        double boosted = clamp01(baseVolume) * DB_FACTOR;
        return clamp01(boosted);
    }

    /**
     * Begrenzt (Clamps) einen Double-Wert zwischen 0.0 und 1.0.
     *
     * @param value Der Eingabewert.
     * @return Der Wert, der auf den Bereich [0.0, 1.0] beschränkt ist.
     */
    private static double clamp01(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }
}