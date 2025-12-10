package de.cyzetlc.hsbi.game.utils.ui;

import de.cyzetlc.hsbi.game.gui.block.Material;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code ImageAssets} utility class is responsible for centralized management, loading,
 * and caching of all image resources used throughout the game, including UI elements and block textures.
 * <p>
 * Using a static cache prevents repeated disk access and object creation, improving performance.
 *
 * @see Material
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class ImageAssets {
    /**
     * Cache specifically for {@code Image} objects related to game {@code Material}s, keyed by the material type.
     */
    private static HashMap<Material, Image> cachedBlockImages = new HashMap<>();

    /**
     * The general-purpose cache mapping image resource paths (Strings) to their loaded {@code Image} objects.
     */
    private static final Map<String, Image> cache = new HashMap<>();

    /**
     * Retrieves an image resource based on its path.
     * <p>
     * If the image is already in the cache, it is returned immediately. Otherwise, the image is
     * loaded using {@code getImageResource(String)} and added to the cache before being returned.
     *
     * @param path The classpath resource path to the image (e.g., "/assets/image.png").
     * @return The loaded {@code Image} object.
     */
    public static Image get(String path) {
        return cache.computeIfAbsent(path, ImageAssets::getImageResource);
    }

    /**
     * Pre-loads a selection of common image assets into the cache.
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
     * Explicitly adds an {@code Image} to the block image cache, associating it with a specific {@code Material}.
     *
     * @param material The {@code Material} key.
     * @param image The {@code Image} object to cache.
     */
    public static void cacheBlockImage(Material material, Image image) {
        cachedBlockImages.put(material, image);
    }

    /**
     * Retrieves an {@code ImageView} for a specific {@code Material}.
     * <p>
     * If the image is not yet in the block cache, it attempts to load it using the material's
     * texture path and caches it before creating the {@code ImageView}. Returns an empty
     * {@code ImageView} if no image can be found.
     *
     * @param material The {@code Material} whose texture is requested.
     * @return A new {@code ImageView} containing the material's texture.
     */
    public static ImageView getBlockImage(Material material) {
        Image image = cachedBlockImages.get(material);
        if (image == null && material.texturePath != null && !material.texturePath.isEmpty()) {
            image = getImageResource(material.texturePath);
            cachedBlockImages.put(material, image);
        }
        return image == null ? new ImageView() : new ImageView(image);
    }

    /**
     * Loads an image from the classpath resource path.
     * <p>
     * If the specified resource is not found, it returns a dedicated "missing texture" image.
     *
     * @param path The classpath resource path of the image.
     * @return The loaded {@code Image} object, or the missing texture fallback.
     */
    public static Image getImageResource(String path) {
        String fallbackPath = "/assets/missing_texture.png";
        Image image;

        try {
            URL resource = ImageAssets.class.getResource(path);
            if (resource != null) {
                image = new Image(resource.toExternalForm());
            } else {
                image = new Image(ImageAssets.class.getResource(fallbackPath).toExternalForm());
            }
        } catch (Exception e) {
            image = new Image(ImageAssets.class.getResource(fallbackPath).toExternalForm());
        }

        return image;
    }
}
