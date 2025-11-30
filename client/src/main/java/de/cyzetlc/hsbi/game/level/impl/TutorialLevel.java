package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.*;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        platforms.add(new Platform(1318, height - 300, 400, 600, root));
        platforms.add(new Platform(1800, height - 400, 100, 600, root));
        platforms.add(new Platform(2000, height - 400, 500, 600, root));
        platforms.add(new Platform(780, height - 450, 100, 50, root));

        // Lava pools for every gap between ground platforms
        this.addLavaBetweenPlatforms(height);

        // USB-Stick auf der ersten Plattform platziert
        this.blocks.add(new USBStickBlock(new Location(200, height - 360)));
        this.blocks.add(new JumpBoostBlock(new Location(1400, height - 332)));
        this.blocks.add(new ServerBlock(new Location( 1500, height - 428)));
        this.blocks.add(new SpeedBoostBlock(new Location(1600, height - 332)));
        this.blocks.add(new FinishBlock(new Location(2400, height - 490)));
        this.blocks.add(new FolderBlock(new Location(800, height - 482)));
        this.blocks.add(new RobotEnemyBlock(new Location(1475, 564), 0, 0)); // static placement for testing

        // floating platform connects upper islands
        this.blocks.add(new FloatingPlatformBlock(
                new Location(940, height - 340),
                new Location(1224, height - 340),
                120,
                0 // 0 = altes MovingPlatform-Artwork
        ));

        // draw blocks
        for (Block block : this.blocks) {
            block.draw(root);
        }

        for (Platform platform : this.platforms) {
            platform.drawPlatform();
        }
    }

    @Override
    public void onFinish() {
        super.onFinish();
        Game.getInstance().getConfig().getObject().put("tutorialFinished", true);
        Game.getInstance().getConfig().save();
    }

    private static LavaBlock createLavaColumn(double x, double y, double width, double height) {
        LavaBlock lava = new LavaBlock(new Location(x, y));
        lava.setWidth(width);
        lava.setHeight(height);
        return lava;
    }

    private void addLavaBetweenPlatforms(double sceneHeight) {
        double lavaTop = sceneHeight - 80;
        double lavaHeight = 300;

        List<Platform> ordered = new ArrayList<>();
        for (Platform platform : this.platforms) {
            boolean nearGround = platform.getY() >= sceneHeight - 400;
            boolean tallEnough = platform.getHeight() >= 400;
            if (nearGround || tallEnough) {
                ordered.add(platform);
            }
        }
        ordered.sort(Comparator.comparingDouble(Platform::getX));

        for (int i = 0; i < ordered.size() - 1; i++) {
            Platform current = ordered.get(i);
            Platform next = ordered.get(i + 1);

            double gapStart = current.getX() + current.getWidth();
            double gapWidth = next.getX() - gapStart;

            if (gapWidth > 1) {
                this.blocks.add(createLavaColumn(gapStart, lavaTop, gapWidth, lavaHeight));
            }
        }
    }

    @Override
    public void update() {

    }
}
