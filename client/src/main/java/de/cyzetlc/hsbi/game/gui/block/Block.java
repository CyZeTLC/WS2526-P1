package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
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

    @Getter
    private Pane pane;

    @Getter
    private ImageView sprite;

    @Getter @Setter
    private Location location;

    @Getter @Setter
    private boolean collideAble;

    @Getter @Setter
    private double width, height;

    @Getter @Setter
    private boolean active;

    @Getter
    private double deltaX, deltaY;

    public Block(Location location) {
        this.location = location;
        this.width = 32;
        this.height = 32;
        this.collideAble = true;
        this.active = true;
        this.deltaX = 0;
        this.deltaY = 0;
    }

    public abstract void onCollide(Player player);

    public void update() {
        this.sprite.setX(this.getLocation().getX() - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraX());
        this.sprite.setY(this.getLocation().getY() - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraY());
    }

    public void draw(Pane pane) {
        /*Rectangle2D rec = new Rectangle();
        rec.setFrame(this.location.getX(), this.location.getY(), 32, 32);*/

        Image image = new Image(getClass().getResource(this.getMaterial().texturePath).toExternalForm());
        this.sprite = new ImageView(image);
        this.pane = pane;

        double spriteWidth = this.width > 0 ? this.width : 32;
        double spriteHeight = this.height > 0 ? this.height : 32;

        // Spritegroesse anhand der Blockabmessungen setzen
        this.sprite.setFitWidth(spriteWidth);
        this.sprite.setFitHeight(spriteHeight);

        // Startposition
        this.sprite.setX(this.getLocation().getX() - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraX());
        this.sprite.setY(this.getLocation().getY() - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraY());

        // Bounding Box (fuer Kollisionen)
        this.setWidth((float) this.sprite.getFitWidth());
        this.setHeight((float) this.sprite.getFitHeight());

        pane.getChildren().add(sprite);
    }

    public void setActive(boolean active) {
        this.active = active;

        if (!active) {
            this.pane.getChildren().remove(this.sprite);
        }
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(this.getLocation().getX(), this.getLocation().getY(), width, height);
    }

    protected void setDelta(double deltaX, double deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
}




