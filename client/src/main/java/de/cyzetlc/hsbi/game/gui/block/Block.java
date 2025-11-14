package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;

public abstract class Block {
    @Getter @Setter
    private Material material;

    private ImageView sprite;

    @Getter @Setter
    private Location location;

    @Getter @Setter
    private boolean collideAble;

    @Getter @Setter
    private double width, height;

    public Block(Location location) {
        this.location = location;
        this.width = 32;
        this.height = 32;
        this.collideAble = true;
    }

    public abstract void onCollide(Player player);

    public abstract void update();

    public void draw(Pane pane) {
        /*Rectangle2D rec = new Rectangle();
        rec.setFrame(this.location.getX(), this.location.getY(), 32, 32);*/

        javafx.scene.image.Image image = new Image(getClass().getResource(this.getMaterial().texturePath).toExternalForm());
        this.sprite = new ImageView(image);

        // Größe anpassen
        this.sprite.setFitWidth(32);
        this.sprite.setFitHeight(32);

        // Startposition
        this.sprite.setX(this.getLocation().getX());
        this.sprite.setY(this.getLocation().getY());

        // Bounding Box (für Kollisionen)
        this.setWidth((float) this.sprite.getFitWidth());
        this.setHeight((float) this.sprite.getFitHeight());

        pane.getChildren().add(sprite);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(this.getLocation().getX(), this.getLocation().getY(), width, height);
    }
}
