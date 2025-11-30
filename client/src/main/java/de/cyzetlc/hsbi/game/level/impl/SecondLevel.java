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

        platforms.add(new Platform(4100, height - 320, 200, 570, root));
        platforms.add(new Platform(4400, height - 360, 250, 610, root));
        platforms.add(new Platform(4800, height - 300, 900, 550, root));

        this.addLavaBetweenPlatforms(height);
        this.blocks.add(new FlipperBlock(new Location(220, height - 320)));
        this.blocks.add(new GasBarrierBlock(new Location(4550, height - 300 - 128), 64, 128));
        // Boss robot on final platform
        this.blocks.add(new RobotEnemyBlock(new Location(5000, height - 300 - 64), 500, 180));
        this.blocks.add(new JumpBoostBlock(new Location(3400, height - 300 - 32)));
        // moving platform at mid height similar to tutorial
        this.blocks.add(new FloatingPlatformBlock(
                new Location(1060, height - 430),
                new Location(1360, height - 430),
                120,
                0
        ));

// --- FLYING PLATFORMS (schwebend) ---

// frühe Schwebefläche
        platforms.add(new Platform(900, height - 550, 200, 50, root));     // mid-air
        blocks.add(new FolderBlock(new Location(980, height - 580)));       // Coin darauf

// über Lava-Sektion
        platforms.add(new Platform(2500, height - 600, 250, 250, root));    // hoch
        blocks.add(new FolderBlock(new Location(2600, height - 630)));      // Coin

// dreifach-Sprung Kette
        platforms.add(new Platform(3600, height - 500, 150, 200, root));
        platforms.add(new Platform(3800, height - 450, 150, 200, root));
        platforms.add(new Platform(4000, height - 500, 150, 200, root));
        blocks.add(new JumpBoostBlock(new Location(3600, height - 470)));   // Boost öffnet neue Route


// --- LAVA COLUMNS (Gefahr) ---
        blocks.add(createLavaColumn(2000, height - 200, 80, 200));
        blocks.add(createLavaColumn(2400, height - 220, 80, 220));


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


        // draw blocks
        for (Block block : this.blocks) {
            block.draw(root);
        }

        for (Platform platform : this.platforms) {
            platform.drawPlatform();
        }
    }

    private void addLavaBetweenPlatforms(double sceneHeight) {
        double lavaTop = sceneHeight - 70;
        double lavaHeight = 250;

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
