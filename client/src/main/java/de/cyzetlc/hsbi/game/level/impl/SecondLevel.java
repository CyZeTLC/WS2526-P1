package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.*;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SecondLevel extends Level {
    private boolean spawnSoundPlayed = false;

    public SecondLevel() {
        super("Second");
    }

    @Override
    public void draw(double width, double height, Pane root) {
        if (!spawnSoundPlayed) {
            SoundManager.play(Sound.ZIEL_ERREICHT, 0.8);
            spawnSoundPlayed = true;
        }

        // --- PLATFORM SECTION (bodennahe Plattformen) ---
        platforms.add(new Platform(0, height - 250, 500, 550, root));        // Start
        platforms.add(new Platform(600, height - 300, 300, 600, root));
        platforms.add(new Platform(1100, height - 350, 400, 650, root));
        platforms.add(new Platform(1700, height - 400, 350, 700, root));

        platforms.add(new Platform(2200, height - 280, 600, 530, root));
        platforms.add(new Platform(2900, height - 330, 300, 580, root));
        platforms.add(new Platform(3300, height - 260, 700, 510, root));

        platforms.add(new Platform(4800, height - 300, 900, 550, root));

        // Flipper-Schlüssel vor der Schranke platzieren
        this.blocks.add(new FlipperItem(new Location(220, height - 320)));

        // Boss robot on final platform (platform top = height - 300)
        this.blocks.add(new RobotEnemyBlock(new Location(5000, height - 300 - 96), 500, 180));
        this.blocks.add(new JumpBoostBlock(new Location(3400, height - 300 - 32)));

        // --- FLYING PLATFORMS (schwebend) ---

        // frühe Schwebefläche
        platforms.add(new Platform(900, height - 550, 200, 50, root));     // mid-air
        blocks.add(new FolderBlock(new Location(980, height - 580)));       // Coin darauf

        // über Lava-Sektion
        platforms.add(new Platform(2500, height - 600, 250, 250, root));    // hoch
        blocks.add(new FolderBlock(new Location(2600, height - 630)));      // Coin

        // dreifach-Sprung Kette
        platforms.add(new Platform(3600, height - 410, 150, 110, root));
        platforms.add(new Platform(3800, height - 450, 150, 200, root));
        platforms.add(new Platform(4000, height - 500, 150, 200, root));

        this.platforms.add(new Platform(4200, height-300, 400, 500, root));
        //this.blocks.add(new GasBarrierBlock(new Location(4550, height - 300 - 128), 64, 128));

        // --- BLOCKS / ITEMS ---
        blocks.add(new FolderBlock(new Location(150, height - 282)));
        blocks.add(new FolderBlock(new Location(650, height - 330)));
        blocks.add(new FolderBlock(new Location(1200, height - 382)));
        blocks.add(new FolderBlock(new Location(1750, height - 432)));
        blocks.add(new FolderBlock(new Location(2350, height - 310)));
        blocks.add(new FolderBlock(new Location(3000, height - 362)));
        blocks.add(new FolderBlock(new Location(3600, height - 530)));  // schwer erreichbare Coin
        blocks.add(new FolderBlock(new Location(4850, height - 330)));

        blocks.add(new JumpBoostBlock(new Location(2000, height - 350)));  // Mid-level

        // --- FINISH ---
        blocks.add(new FinishBlock(new Location(4800 + 900 - 150, height - 390)));

        //this.placeLavaBetweenPlatforms(height);

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
        return new BossLevel();
    }
}
