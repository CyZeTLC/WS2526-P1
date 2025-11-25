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
        platforms.add(new Platform(4800, height - 300, 400, 550, root));


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
        blocks.add(createLavaColumn(3200, height - 180, 100, 180));


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
        blocks.add(new FinishBlock(new Location(5000, height - 390)));


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
