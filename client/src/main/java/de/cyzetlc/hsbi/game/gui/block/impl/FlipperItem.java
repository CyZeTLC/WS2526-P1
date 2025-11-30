package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

/**
 * Einsammelbares Flipper-Item (Schlüssel) zum Deaktivieren der Gas-Barriere.
 * - Wird nur gezeichnet, solange es nicht eingesammelt wurde.
 * - Bei Kollision setzt es ein Flag am Spieler und verschwindet.
 */
public class FlipperItem extends Block {
    private boolean collected = false;

    public FlipperItem(Location location) {
        super(location);
        this.setMaterial(Material.FLIPPER);
        this.setCollideAble(false); // kein Blockieren der Bewegung
        this.setWidth(64);
        this.setHeight(64);
    }

    @Override
    public void draw(Pane pane) {
        if (collected) return; // nicht zeichnen, wenn eingesammelt
        super.draw(pane);
        this.sprite.setPreserveRatio(true);
        this.sprite.setFitWidth(this.getWidth());
        this.sprite.setFitHeight(this.getHeight());
    }

    @Override
    public void onCollide(Player player) {
        // Keine Kollision nötig; Einsammeln wird separat geprüft.
    }

    /**
     * Muss pro Frame aus dem Level-Update aufgerufen werden.
     */
    public void update(Player player) {
        if (collected || player == null) return;
        // Einfache Rechteck-Kollision gegen Spieler-Hitbox
        boolean intersect = player.getLocation().getX() + player.getWidth() >= this.getLocation().getX()
                && player.getLocation().getX() <= this.getLocation().getX() + this.getWidth()
                && player.getLocation().getY() + player.getHeight() >= this.getLocation().getY()
                && player.getLocation().getY() <= this.getLocation().getY() + this.getHeight();

        if (intersect) {
            collected = true;
            player.setFlipperCollected(true); // Schlüssel-Flag setzen
            this.setActive(false);
            if (this.getPane() != null) {
                this.getPane().getChildren().remove(this.sprite);
            }
        }
    }

    /**
     * Optional: HUD-Icon anzeigen, wenn Spieler den Flipper hat.
     */
    public static void renderHudIfCollected(Pane hudPane, boolean hasFlipper) {
        if (!hasFlipper) return;
        // Kleines Icon oben links im HUD, nur wenn nicht schon im Pane
        var img = new javafx.scene.image.Image(FlipperItem.class.getResource(Material.FLIPPER.texturePath).toExternalForm());
        var view = new javafx.scene.image.ImageView(img);
        view.setFitWidth(32);
        view.setFitHeight(32);
        view.setX(12);
        view.setY(12);
        if (!hudPane.getChildren().contains(view)) {
            hudPane.getChildren().add(view);
        }
    }
}
