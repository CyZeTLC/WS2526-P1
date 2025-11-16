package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.FloatingPlatformBlock;
import de.cyzetlc.hsbi.game.gui.block.JumpBoostBlock;
import de.cyzetlc.hsbi.game.gui.block.LavaBlock;
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
        platforms.add(new Platform(0, height - 300, 450, 300, root));
        platforms.add(new Platform(500, height - 350, 200, 350, root));
        platforms.add(new Platform(780, height - 300, 150, 300, root));

        this.blocks.add(new JumpBoostBlock(new Location(150, height - 332)));

        // floating platform connects upper islands
        this.blocks.add(new FloatingPlatformBlock(
                new Location(420, height - 420),
                new Location(720, height - 420),
                120
        ));

        // draw blocks
        this.fillGapsWithLava(height);
        for (Block block : this.blocks) {
            block.draw(root);
        }
    }

    private void fillGapsWithLava(double screenHeight) {
        // gap between platform 1 (0-450) and 2 (500-700)
        this.blocks.add(createLavaColumn(450, screenHeight - 300, 50, 300));

        // gap between platform 2 (500-700) and 3 (780-930)
        this.blocks.add(createLavaColumn(700, screenHeight - 350, 80, 350));
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
