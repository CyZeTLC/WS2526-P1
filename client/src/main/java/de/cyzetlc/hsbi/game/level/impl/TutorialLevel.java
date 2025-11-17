package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.FloatingPlatformBlock;
import de.cyzetlc.hsbi.game.gui.block.impl.JumpBoostBlock;
import de.cyzetlc.hsbi.game.gui.block.impl.LavaBlock;
import de.cyzetlc.hsbi.game.gui.block.impl.ServerBlock;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

public class TutorialLevel extends Level {
    public TutorialLevel() {
        super("Tutorial");
    }

    @Override
    public void draw(double width, double height, Pane root) {
        // example platforms (placeholder layout)
        platforms.add(new Platform(0, height - 300, 450, 600, root));
        platforms.add(new Platform(500, height - 350, 200, 650, root));
        platforms.add(new Platform(780, height - 300, 150, 600, root));
        platforms.add(new Platform(1318, height - 300, 1900, 600, root));

        this.blocks.add(new JumpBoostBlock(new Location(150, height - 332)));
        this.blocks.add(new ServerBlock(new Location( 1500, height - 428)));

        // floating platform connects upper islands
        this.blocks.add(new FloatingPlatformBlock(
                new Location(940, height - 340),
                new Location(1224, height - 340),
                120,
                0 // 0 = altes MovingPlatform-Artwork
        ));

        // draw blocks
        this.fillGapsWithLava(height);
        for (Block block : this.blocks) {
            block.draw(root);
        }

        for (Platform platform : this.platforms) {
            platform.drawPlatform();
        }
    }

    private void fillGapsWithLava(double screenHeight) {
        // gap between platform 1 (0-450) and 2 (500-700)
        this.blocks.add(createLavaColumn(450, screenHeight - 280, 50, 600));

        // gap between platform 2 (500-700) and 3 (780-930)
        this.blocks.add(createLavaColumn(700, screenHeight - 280, 80, 600));
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
