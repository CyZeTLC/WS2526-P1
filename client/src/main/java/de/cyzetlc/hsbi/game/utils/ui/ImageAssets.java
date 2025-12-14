package de.cyzetlc.hsbi.game.utils.ui;

import de.cyzetlc.hsbi.game.gui.block.Material;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Die Utility-Klasse {@code ImageAssets} ist verantwortlich für die zentralisierte Verwaltung, das Laden
 * und das Caching aller Bildressourcen, die im gesamten Spiel verwendet werden, einschließlich UI-Elementen und Blocktexturen.
 *
 * <p>
 * Die Verwendung eines statischen Caches verhindert wiederholten Festplattenzugriff und Objekt-Erstellung,
 * was die Performance verbessert.
 *
 * @see Material
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class ImageAssets {
    /**
     * Cache speziell für {@code Image}-Objekte, die zu Spiel-{@code Material}ien gehören, geschlüsselt nach dem Materialtyp.
     */
    private static HashMap<Material, Image> cachedBlockImages = new HashMap<>();

    /**
     * Der allgemeine Cache, der Bildressourcenpfade (Strings) auf die geladenen {@code Image}-Objekte abbildet.
     */
    private static final Map<String, Image> cache = new HashMap<>();

    /**
     * Ruft eine Bildressource basierend auf ihrem Pfad ab.
     * <p>
     * Wenn das Bild bereits im Cache vorhanden ist, wird es sofort zurückgegeben. Andernfalls wird das Bild
     * mit {@code getImageResource(String)} geladen und dem Cache hinzugefügt, bevor es zurückgegeben wird.
     * Dies implementiert das "Compute-If-Absent"-Muster für effizientes Caching.
     *
     * @param path Der Classpath-Ressourcenpfad zum Bild (z. B. "/assets/image.png").
     * @return Das geladene {@code Image}-Objekt.
     */
    public static Image get(String path) {
        return cache.computeIfAbsent(path, ImageAssets::getImageResource);
    }

    /**
     * Lädt eine Auswahl gängiger Bildressourcen vorab in den Cache (Warm-up).
     * <p>
     * Dies reduziert Latenzen beim ersten Zugriff auf diese Bilder während des Spiels.
     */
    public static void warm() {
        String[] paths = {
                "/assets/hud/background.png",
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png",
                "/assets/hud/heart_full.png",
                "/assets/hud/heart_half.png",
                "/assets/hud/heart_empty.png",
                "/assets/movingplatform/1MovingPlatform32x64.png",
                "/assets/movingplatform/zuschnitt1.png",
                "/assets/movingplatform/zuschnitt1_zustand1.png"
        };

        for (String p : paths) {
            get(p);
        }
    }

    /**
     * Fügt explizit ein {@code Image} zum Block-Image-Cache hinzu und assoziiert es mit einem spezifischen {@code Material}.
     *
     * @param material Der {@code Material}-Schlüssel.
     * @param image Das zu cachende {@code Image}-Objekt.
     */
    public static void cacheBlockImage(Material material, Image image) {
        cachedBlockImages.put(material, image);
    }

    /**
     * Ruft einen {@code ImageView} für ein spezifisches {@code Material} ab.
     * <p>
     * Ist das Bild noch nicht im Block-Cache, versucht die Methode, es über den Texturpfad des Materials zu laden
     * und cached es, bevor der {@code ImageView} erstellt wird. Gibt einen leeren
     * {@code ImageView} zurück, falls kein Bild gefunden werden kann.
     *
     * @param material Das {@code Material}, dessen Textur angefordert wird.
     * @return Ein neues {@code ImageView}, das die Textur des Materials enthält.
     */
    public static ImageView getBlockImage(Material material) {
        Image image = cachedBlockImages.get(material);
        // Versuch, das Bild zu laden und zu cachen, wenn es fehlt und ein Pfad existiert
        if (image == null && material.texturePath != null && !material.texturePath.isEmpty()) {
            image = getImageResource(material.texturePath);
            cachedBlockImages.put(material, image);
        }
        // Gib entweder das ImageView mit dem Bild oder ein leeres ImageView zurück
        return image == null ? new ImageView() : new ImageView(image);
    }

    /**
     * Lädt ein Bild über den Classpath-Ressourcenpfad.
     * <p>
     * Wird die angegebene Ressource nicht gefunden, gibt die Methode ein dediziertes "Missing Texture"-Bild zurück.
     *
     * @param path Der Classpath-Ressourcenpfad des Bildes.
     * @return Das geladene {@code Image}-Objekt oder das Fallback-Bild für fehlende Texturen.
     */
    public static Image getImageResource(String path) {
        String fallbackPath = "/assets/missing_texture.png";
        Image image;

        try {
            URL resource = ImageAssets.class.getResource(path);
            if (resource != null) {
                // Lade das gefundene Bild
                image = new Image(resource.toExternalForm());
            } else {
                // Ressource nicht gefunden, lade Fallback
                System.err.println("WARN: Image resource not found: " + path);
                image = new Image(ImageAssets.class.getResource(fallbackPath).toExternalForm());
            }
        } catch (Exception e) {
            // Fehler beim Laden (z.B. falsche URL), lade Fallback
            System.err.println("ERROR loading image resource " + path + ": " + e.getMessage());
            image = new Image(ImageAssets.class.getResource(fallbackPath).toExternalForm());
        }

        return image;
    }
}