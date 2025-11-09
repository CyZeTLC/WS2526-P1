package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import lombok.Getter;

@Getter
public class EntityPlayer extends Player {
    private Circle circle;
    private Text nameTag;

    @Override
    public void update() {
        this.circle.setCenterX(this.getLocation().getX());
        this.circle.setCenterY(this.getLocation().getY());
        this.nameTag.setX(this.getLocation().getX()-this.nameTag.getLayoutBounds().getWidth()/2-10);
        this.nameTag.setY(this.getLocation().getY()-25);
    }

    public EntityPlayer drawPlayer(Pane pane, double x, double y) {
        this.nameTag = UIUtils.drawText(pane, this.getDisplayName(), x, y-25);
        this.nameTag.setX(x-this.nameTag.getLayoutBounds().getWidth()/2);

        this.circle = new Circle(200, 150, 20, Color.CYAN);
        this.setLocation(new Location(x,y));
        this.circle.setCenterX(x);
        this.circle.setCenterY(y);
        pane.getChildren().add(this.circle);
        return this;
    }
}
