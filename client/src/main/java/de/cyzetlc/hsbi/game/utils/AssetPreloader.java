package de.cyzetlc.hsbi.game.utils;

import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Laedt Assets im Hintergrund vor, damit der erste Start des GameScreens schneller wirkt.
 */
public final class AssetPreloader {

    private static final AtomicBoolean started = new AtomicBoolean(false);
    private static CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

    private AssetPreloader() {
    }

    public static CompletableFuture<Void> start() {
        if (started.compareAndSet(false, true)) {
            future = CompletableFuture.runAsync(() -> {
                warmImages();
                SoundManager.preloadAll();
            });
        }
        return future;
    }

    public static CompletableFuture<Void> getFuture() {
        return future;
    }

    private static void warmImages() {
        List<String> paths = List.of(
                "/assets/hud/background.png",
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png",
                "/assets/hud/heart_full.png",
                "/assets/hud/heart_half.png",
                "/assets/hud/heart_empty.png",
                "/assets/movingplatform/1MovingPlatform32x64.png",
                "/assets/movingplatform/zuschnitt1.png",
                "/assets/movingplatform/zuschnitt1_zustand1.png"
        );

        for (String path : paths) {
            try {
                InputStream stream = UIUtils.class.getResourceAsStream(path);
                if (stream != null) {
                    new Image(stream);
                }
            } catch (Exception ignored) {
                // Wenn einzelne Assets fehlen, laden wir den Rest trotzdem.
            }
        }
    }
}
