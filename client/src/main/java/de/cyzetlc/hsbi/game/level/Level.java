package de.cyzetlc.hsbi.game.level;

import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.LavaBlock;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public abstract class Level {
    protected String name;

    protected final List<Platform> platforms;

    protected final List<Block> blocks;

    private long levelStarted;

    @Getter
    private Level nextLevel;

    public Level(String name) {
        this.name = name;

        this.platforms = new ArrayList<>();
        this.blocks =  new ArrayList<>();
        this.levelStarted = System.currentTimeMillis();
    }

    public void onFinish() { }

    public abstract void draw(double width, double height, Pane root);

    public abstract void update();

    /**
     * Fügt einen Lava-Block (Block) in jede horizontale Lücke zwischen aufeinanderfolgenden Plattformen ein.
     * Es wird davon ausgegangen, dass 'this.platforms' eine List von Platform-Objekten und
     * 'this.blocks' eine List von Block-Objekten ist.
     * Der Lava-Block wird an einer festen vertikalen Position platziert.
     *
     * @param sceneHeight Die Gesamthöhe der Spielszene, oft zur Definition des Bodens verwendet.
     */
    protected void placeLavaBetweenPlatforms(double sceneHeight) {
        final double LAVA_TOP_Y = sceneHeight - 70.0;
        final double LAVA_HEIGHT = 250.0;

        List<Platform> orderedPlatforms = new ArrayList<>(this.platforms);

        orderedPlatforms.sort(Comparator.comparingDouble(Platform::getX));

        if (orderedPlatforms.size() < 2) {
            return;
        }

        for (int i = 0; i < orderedPlatforms.size() - 1; i++) {
            Platform currentPlatform = orderedPlatforms.get(i);
            Platform nextPlatform = orderedPlatforms.get(i + 1);

            double gapStart = currentPlatform.getX() + currentPlatform.getWidth();

            double gapWidth = nextPlatform.getX() - gapStart;

            if (gapWidth > 1.0) {
                this.blocks.add(createLavaColumn(gapStart, LAVA_TOP_Y, gapWidth, LAVA_HEIGHT));
            }
        }
    }

    protected static LavaBlock createLavaColumn(double x, double y, double width, double height) {
        LavaBlock lava = new LavaBlock(new Location(x, y));
        lava.setWidth(width);
        lava.setHeight(height);
        return lava;
    }
}
