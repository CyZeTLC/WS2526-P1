package de.cyzetlc.hsbi.game.level;

import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Block;
import javafx.scene.layout.Pane;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Level {
    protected String name;

    protected final List<Platform> platforms;

    protected final List<Block> blocks;

    private long levelStarted;

    public Level(String name) {
        this.name = name;

        this.platforms = new ArrayList<>();
        this.blocks =  new ArrayList<>();
        this.levelStarted = System.currentTimeMillis();
    }

    public void onFinish() { }

    public abstract void draw(double width, double height, Pane root);

    public abstract void update();
}
