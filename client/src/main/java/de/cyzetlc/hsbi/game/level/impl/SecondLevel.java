package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.*;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

public class SecondLevel extends Level {
    public SecondLevel() {
        super("Second");
    }

    @Override
    public void draw(double width, double height, Pane root) {
        // example platforms (placeholder layout)
        platforms.add(new Platform(0, height - 300, 450, 600, root));
        platforms.add(new Platform(500, height - 350, 200, 650, root));
        platforms.add(new Platform(1800, height - 400, 100, 600, root));
        platforms.add(new Platform(2000, height - 400, 500, 600, root));

        // draw blocks
        for (Block block : this.blocks) {
            block.draw(root);
        }

        for (Platform platform : this.platforms) {
            platform.drawPlatform();
        }
    }

    private static LavaBlock createLavaColumn(double x, double y, double width, double height) {
        LavaBlock lava = new LavaBlock(new Location(x, y));
        lava.setWidth(width);
        lava.setHeight(height);
        return lava;
    }

    @Override
    public void update() {

    }
}
