package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Direction;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;

/**
 * The {@code EntityPlayer} class represents the main player character entity in the game world.
 * It extends the base {@code Player} class and adds visual elements (sprite, name tag)
 * and specific logic for rendering and updating the player's position relative to the camera.
 *
 * @see Player
 * @see GameScreen
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
@Getter
public class EntityPlayer extends Player {
    /**
     * The JavaFX {@code ImageView} used to render the player's visual sprite.
     */
    private ImageView sprite;
    /**
     * The JavaFX {@code Text} node used to display the player's name tag above the sprite.
     */
    private Text nameTag;

    /**
     * The current horizontal direction of movement (e.g., WALK_LEFT or WALK_RIGHT), used
     * to determine sprite mirroring.
     */
    @Setter
    private Direction direction;

    /**
     * Flag indicating whether the player is currently able to collect game files or objectives.
     */
    @Setter
    private boolean canCollectFiles = false;

    /**
     * Constructs a new {@code EntityPlayer} instance.
     */
    public EntityPlayer() {
        super();
    }

    /**
     * Updates the player's visual components (sprite and name tag) based on the player's
     * world location and the current camera offset.
     * <p>
     * This method ensures the player's screen position is correct for smooth scrolling and
     * handles sprite mirroring based on the {@code direction} attribute.
     */
    @Override
    public void update() {
        double camX = 0;
        double camY = 0;

        if (Game.getInstance() != null && Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen gameScreen) {
            camX = gameScreen.getCameraX();
            camY = gameScreen.getCameraY();
        }

        // Position vom Sprite & Nametag anpassen (mit Kameraversatz)
        double screenX = this.getLocation().getX() - camX;
        double screenY = this.getLocation().getY() - camY;

        this.sprite.setX(screenX);
        this.sprite.setY(screenY);

        double tagWidth = this.nameTag.prefWidth(-1);

        double centeredX = screenX + (this.getWidth() / 2) - (tagWidth / 2);
        double centeredY = screenY - 25;

        this.nameTag.setLayoutX(centeredX);
        this.nameTag.setLayoutY(centeredY);

        // Richtung wechseln
        if (direction != null) {
            switch (direction) {
                case WALK_LEFT -> sprite.setScaleX(-1);  // Spiegeln nach links
                case WALK_RIGHT -> sprite.setScaleX(1);   // Normal (nach rechts)
            }
        }
    }

    /**
     * Initializes and draws the player's visual sprite and name tag onto the game pane.
     * <p>
     * Sets the initial location, size, and collision bounding box for the entity.
     *
     * @param pane The {@code Pane} (the game root) where the player elements should be added.
     * @param x The initial world X-coordinate.
     * @param y The initial world Y-coordinate.
     * @return The current {@code EntityPlayer} instance for chaining.
     */
    public EntityPlayer drawPlayer(Pane pane, double x, double y) {
        // Nametag zeichnen
        this.nameTag = UIUtils.drawText(pane, this.getDisplayName(), x, y - 25);
        this.nameTag.setLayoutX(x - this.nameTag.getLayoutBounds().getWidth() / 2);

        // Bild laden
        Image image = new Image(getClass().getResource("/assets/player.png").toExternalForm());
        this.sprite = new ImageView(image);

        // Größe anpassen
        this.sprite.setFitWidth(40);
        this.sprite.setFitHeight(60);

        // Startposition
        this.sprite.setX(x);
        this.sprite.setY(y);

        // Bounding Box (für Kollisionen)
        this.setWidth((float) this.sprite.getFitWidth());
        this.setHeight((float) this.sprite.getFitHeight());
        this.setLocation(new Location(x, y));

        pane.getChildren().add(this.sprite);
        return this;
    }
}
