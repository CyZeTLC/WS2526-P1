package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.Game;
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

/**
 * Die Klasse {@code SecondLevel} implementiert den zweiten Spielabschnitt.
 * <p>
 * Dieses Level zeichnet eine Abfolge von Plattformen mit variierenden Höhen und Abständen,
 * enthält sammelbare Gegenstände ({@code FolderBlock}), einen Sprung-Boost ({@code JumpBoostBlock}),
 * einen Gegner ({@code RobotEnemyBlock}) und Lava-Gruben (optional, kommentiert)
 * sowie den Übergang zum nächsten Level.
 *
 * @author Tom Coombs
 * @author leonardo (aka. Phantomic)
 *
 * @see Level
 * @see BossLevel
 */
public class SecondLevel extends Level {
    /**
     * Flag, das verfolgt, ob der Sound beim Laden des Levels bereits abgespielt wurde.
     */
    private boolean spawnSoundPlayed = false;

    /**
     * Konstruiert ein neues SecondLevel.
     */
    public SecondLevel() {
        super("Second");
        Game.thePlayer.setCanCollectFiles(true);
    }

    /**
     * Zeichnet die statischen und dynamischen Elemente des SecondLevels auf das Root-Pane.
     * <p>
     * Enthält primäre Bodenplattformen, schwebende Plattformen, sammelbare FolderBlocks und spezielle Blöcke.
     *
     * @param width Die Breite des Anzeigebereichs.
     * @param height Die Höhe des Anzeigebereichs.
     * @param root Das Root-Pane, in das die Elemente gezeichnet werden.
     */
    @Override
    public void draw(double width, double height, Pane root) {
        // Spielen eines Sounds beim Laden des Levels
        if (!spawnSoundPlayed) {
            SoundManager.play(Sound.ZIEL_ERREICHT, 0.8);
            spawnSoundPlayed = true;
        }

        // --- PLATTFORM-ABSCHNITT (bodennahe Plattformen) ---
        // Die Y-Werte der Plattformen variieren (höher/tiefer) für unterschiedliche Sprungherausforderungen
        platforms.add(new Platform(0, height - 250, 500, 550, root));        // Start
        platforms.add(new Platform(600, height - 300, 300, 600, root));
        platforms.add(new Platform(1100, height - 350, 400, 650, root));
        platforms.add(new Platform(1700, height - 400, 350, 700, root));

        platforms.add(new Platform(2200, height - 280, 600, 530, root));
        platforms.add(new Platform(2900, height - 330, 300, 580, root));
        platforms.add(new Platform(3300, height - 260, 700, 510, root));

        platforms.add(new Platform(4800, height - 300, 900, 550, root)); // Endplattform

        // Gegner-Platzierung und Boost-Block
        // Boss-Roboter auf der letzten Plattform (Plattform-Oberkante = height - 300)
        final double platformY = height - 300;
        final double robotHeightOffset = 96;
        final double jumpBoostHeightOffset = 32;

        this.blocks.add(new RobotEnemyBlock(new Location(5000, platformY - robotHeightOffset), 500, 180));
        this.blocks.add(new JumpBoostBlock(new Location(3400, platformY - jumpBoostHeightOffset)));

        // --- SCHWEBENDE PLATTFORMEN (mid-air) ---

        // frühe Schwebefläche
        platforms.add(new Platform(900, height - 550, 200, 50, root));     // mid-air
        blocks.add(new FolderBlock(new Location(980, height - 580)));       // Coin darauf

        // über Lava-Sektion (wäre hier)
        platforms.add(new Platform(2500, height - 600, 250, 250, root));    // hoch
        blocks.add(new FolderBlock(new Location(2600, height - 630)));      // Coin

        // dreifach-Sprung Kette (Plattformen in ansteigender Höhe)
        platforms.add(new Platform(3600, height - 410, 150, 110, root));
        platforms.add(new Platform(3800, height - 450, 150, 200, root));
        platforms.add(new Platform(4000, height - 500, 150, 200, root));

        // Plattform vor Gas-Barriere (kommentiert)
        this.platforms.add(new Platform(4200, height-300, 400, 500, root));
        //this.blocks.add(new GasBarrierBlock(new Location(4550, height - 300 - 128), 64, 128));

        // --- BLÖCKE / ITEMS (Sammelobjekte) ---
        // Die Coins (FolderBlocks) sind strategisch auf den Plattformen platziert
        blocks.add(new FolderBlock(new Location(150, height - 282)));
        blocks.add(new FolderBlock(new Location(650, height - 330)));
        blocks.add(new FolderBlock(new Location(1200, height - 382)));
        blocks.add(new FolderBlock(new Location(1750, height - 432)));
        blocks.add(new FolderBlock(new Location(2350, height - 310)));
        blocks.add(new FolderBlock(new Location(3000, height - 362)));
        blocks.add(new FolderBlock(new Location(3600, height - 442)));  // schwer erreichbare Coin
        blocks.add(new FolderBlock(new Location(4850, height - 330)));

        // --- ZIEL (FINISH) ---
        // Platzierung des Finish-Blocks am Ende der letzten Plattform
        blocks.add(new FinishBlock(new Location(4800 + 900 - 150, height - 390)));

        // --- LAVA (optional) ---
        // Lücken zwischen den Plattformen mit Lava füllen (derzeit auskommentiert)
        //this.placeLavaBetweenPlatforms(height);

        // Zeichne alle Plattformen
        for (Platform platform : this.platforms) {
            platform.drawPlatform();
        }

        // Zeichne alle Blöcke
        for (Block block : this.blocks) {
            block.draw(root);
        }
    }

    /**
     * Aktualisiert die Level-Logik.
     * Derzeit leer.
     */
    @Override
    public void update() {

    }

    /**
     * Gibt das nächste Level in der Abfolge zurück, welches das BossLevel ist.
     *
     * @return Eine neue Instanz des {@code BossLevel}.
     */
    @Override
    public Level getNextLevel() {
        return new BossLevel();
    }
}