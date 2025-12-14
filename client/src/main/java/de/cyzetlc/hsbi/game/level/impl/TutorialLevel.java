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

/**
 * Die Klasse {@code TutorialLevel} implementiert den ersten Spielabschnitt, der dem Spieler
 * die grundlegenden Mechanismen und Blöcke des Spiels näherbringt.
 * <p>
 * Das Level enthält eine Reihe von statischen Plattformen, eine bewegliche Plattform ({@code FloatingPlatformBlock})
 * sowie verschiedene interaktive Blöcke wie Sammelobjekte, Boosts und den Ziel-Block.
 *
 * @author Tom Coombs
 *
 * @see Level
 * @see SecondLevel
 */
public class TutorialLevel extends Level {
    /**
     * Konstruiert ein neues TutorialLevel.
     */
    public TutorialLevel() {
        super("Tutorial");
    }

    /**
     * Zeichnet die statischen und dynamischen Elemente des TutorialLevels auf das Root-Pane.
     * <p>
     * Die Anordnung der Plattformen und Blöcke ist auf das Erlernen von Sprüngen und Interaktionen ausgelegt.
     *
     *
     * @param width Die Breite des Anzeigebereichs.
     * @param height Die Höhe des Anzeigebereichs.
     * @param root Das Root-Pane, in das die Elemente gezeichnet werden.
     */
    @Override
    public void draw(double width, double height, Pane root) {
        // Beispiel-Plattformen (Platzhalter-Layout)
        platforms.add(new Platform(0, height - 300, 450, 600, root));    // Startplattform
        platforms.add(new Platform(500, height - 350, 200, 650, root));  // Höhere Plattform
        platforms.add(new Platform(780, height - 300, 150, 600, root));
        platforms.add(new Platform(1318, height - 300, 400, 600, root));
        platforms.add(new Platform(1800, height - 400, 100, 600, root));
        platforms.add(new Platform(2000, height - 400, 500, 600, root)); // Zielplattform
        platforms.add(new Platform(780, height - 450, 100, 50, root));    // Obere Insel

        // --- BLÖCKE / ITEMS ---
        // USB-Stick als wichtiges Sammelobjekt platziert
        this.blocks.add(new USBStickBlock(new Location(200, height - 360)));
        // Boost-Blöcke zur Demonstration
        this.blocks.add(new JumpBoostBlock(new Location(1400, height - 332)));
        this.blocks.add(new ServerBlock(new Location( 1500, height - 428)));
        this.blocks.add(new SpeedBoostBlock(new Location(1600, height - 332)));
        // Zielblock
        this.blocks.add(new FinishBlock(new Location(2400, height - 490)));
        // FolderBlock als sammelbares Tutorial-Item
        this.blocks.add(new FolderBlock(new Location(800, height - 482)));

        // Bewegliche Plattform, die obere Inseln verbindet
        this.blocks.add(new FloatingPlatformBlock(
                new Location(940, height - 340), // Startpunkt
                new Location(1224, height - 340), // Endpunkt
                120, // Geschwindigkeit
                0 // 0 = altes MovingPlatform-Artwork
        ));

        // Fügt Lava in alle Lücken zwischen den Plattformen ein
        this.placeLavaBetweenPlatforms(height);

        // Zeichne Blöcke (vor Plattformen, falls die Plattformen über Blöcke gezeichnet werden sollen)
        for (Block block : this.blocks) {
            block.draw(root);
        }

        // Zeichne Plattformen
        for (Platform platform : this.platforms) {
            platform.drawPlatform();
        }
    }

    /**
     * Wird aufgerufen, wenn das Level erfolgreich abgeschlossen wurde.
     * Speichert den Status, dass das Tutorial beendet ist.
     */
    @Override
    public void onFinish() {
        super.onFinish();
        // Speichere, dass das Tutorial abgeschlossen wurde
        Game.getInstance().getConfig().getObject().put("tutorialFinished", true);
        Game.getInstance().getConfig().save();
    }

    /**
     * Aktualisiert die Level-Logik.
     * Derzeit leer. Die Logik für Blöcke (wie die bewegliche Plattform) wird in den Blöcken selbst behandelt.
     */
    @Override
    public void update() {

    }

    /**
     * Gibt das nächste Level in der Abfolge zurück.
     *
     * @return Eine neue Instanz des {@code SecondLevel}.
     */
    @Override
    public Level getNextLevel() {
        return new SecondLevel();
    }
}