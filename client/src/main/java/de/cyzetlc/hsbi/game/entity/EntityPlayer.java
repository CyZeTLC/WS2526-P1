package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Direction;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;

@Getter
public class EntityPlayer extends Player {
    private ImageView sprite;
    private Text nameTag;

    @Setter
    private Direction direction;

    @Getter @Setter
    private Rectangle2D realLocation;

    public EntityPlayer() {
        super();
    }

    @Override
    public void update() {
        // Position vom Sprite & Nametag anpassen
        this.sprite.setX(this.getLocation().getX());
        this.sprite.setY(this.getLocation().getY());
        this.nameTag.setLayoutX(this.getLocation().getX() + this.nameTag.getLayoutBounds().getWidth() / 2 - this.getWidth()*1.4);
        this.nameTag.setLayoutY(this.getLocation().getY() - 25);

        this.setRealLocation(new Rectangle2D(this.sprite.getX(), this.sprite.getY(), this.getWidth(), this.getHeight()));

        // Richtung wechseln
        if (direction != null) {
            switch (direction) {
                case WALK_LEFT -> sprite.setScaleX(-1);  // Spiegeln nach links
                case WALK_RIGHT -> sprite.setScaleX(1);   // Normal (nach rechts)
            }
        }
    }

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
