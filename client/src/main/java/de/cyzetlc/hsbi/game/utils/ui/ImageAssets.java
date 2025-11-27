package de.cyzetlc.hsbi.game.utils.ui;

import de.cyzetlc.hsbi.game.gui.block.Material;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageAssets {
    private static HashMap<Material, Image> cachedBlockImages = new HashMap<>();

    private static final Map<String, Image> cache = new HashMap<>();

    public static Image get(String path) {
        return cache.computeIfAbsent(path, p ->
                new Image(ImageAssets.class.getResourceAsStream(p))
        );
    }

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

    public static void cacheBlockImage(Material material, Image image) {
        cachedBlockImages.put(material, image);
    }

    public static ImageView getBlockImage(Material material) {
        Image image = cachedBlockImages.get(material);
        if (image == null && material.texturePath != null && !material.texturePath.isEmpty()) {
            image = getImageResource(material.texturePath);
            cachedBlockImages.put(material, image);
        }
        return image == null ? new ImageView() : new ImageView(image);
    }

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
