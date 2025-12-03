package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.FinishBlock;
import de.cyzetlc.hsbi.game.gui.block.impl.RobotEnemyBlock;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

public class CommunityLevel extends Level {
    private boolean spawnSoundPlayed = false;

    public CommunityLevel() {
        super("Community");
    }

    @Override
    public void draw(double width, double height, Pane root) {
        // --- PLATFORM SECTION (bodennahe Plattformen) ---
        platforms.add(new Platform(0, height - 300, 2500, 550, root));        // Start
        platforms.add(new Platform(2500, 0, 50, height, root));

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
