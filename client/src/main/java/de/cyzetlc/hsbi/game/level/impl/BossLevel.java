package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.*;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

public class BossLevel extends Level {
    private boolean spawnSoundPlayed = false;

    public BossLevel() {
        super("Boss");
    }

    @Override
    public void draw(double width, double height, Pane root) {
        platforms.add(new Platform(0, height - 300, 2500, 550, root));        // Start

        this.blocks.add(new RobotEnemyBlock(new Location(250, height - 300 - 96), 500, 180));
        this.blocks.add(new RobotEnemyBlock(new Location(700, height - 300 - 96), 500, 180));
        this.blocks.add(new RobotEnemyBlock(new Location(1050, height - 300 - 96), 500, 180));
        this.blocks.add(new RobotEnemyBlock(new Location(1400, height - 300 - 96), 500, 180));

        blocks.add(new FinishBlock(new Location(1800, height - 390)));

        // draw Platforms
        for (Platform platform : this.platforms) {
            platform.drawPlatform();
        }

        // draw blocks
        for (Block block : this.blocks) {
            block.draw(root);
        }
    }

    @Override
    public void update() {

    }

    @Override
    public Level getNextLevel() {
        return null;
    }
}
