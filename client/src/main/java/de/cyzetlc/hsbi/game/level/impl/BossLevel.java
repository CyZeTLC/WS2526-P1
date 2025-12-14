package de.cyzetlc.hsbi.game.level.impl;

import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.*;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

/**
 * Die Klasse {@code BossLevel} implementiert ein spezifisches Level, das als Bosskampf- oder
 * End-Level dient.
 * <p>
 * Dieses Level zeichnet eine lange Startplattform und platziert mehrere {@code RobotEnemyBlock}-Gegner
 * sowie einen {@code FinishBlock} als Ziel.
 *
 * @author Tom Coombs
 * @author leonardo (aka. Phantomic)
 *
 * @see Level
 * @see Platform
 * @see RobotEnemyBlock
 */
public class BossLevel extends Level {
    /**
     * Flag, das verfolgt, ob der Sound für das Erscheinen des Bosses (oder der Gegner) bereits abgespielt wurde.
     */
    private boolean spawnSoundPlayed = false;

    /**
     * Konstruiert ein neues BossLevel.
     */
    public BossLevel() {
        super("Boss");
    }

    /**
     * Zeichnet die statischen und dynamischen Elemente des BossLevels auf das Root-Pane.
     * <p>
     * Dies umfasst eine große Plattform und die Platzierung von vier Robotergegnern sowie dem Zielblock.
     *
     * @param width Die Breite des Anzeigebereichs.
     * @param height Die Höhe des Anzeigebereichs.
     * @param root Das Root-Pane, in das die Elemente gezeichnet werden.
     */
    @Override
    public void draw(double width, double height, Pane root) {
        // Erstellt eine sehr lange Startplattform, die fast über den gesamten Level reicht
        platforms.add(new Platform(0, height - 300, 2500, 550, root));        // Start

        // Platziert vier Robotergegner auf der Plattform
        final double enemyHeightOffset = 96; // Höhe des Gegners
        final double platformY = height - 300;

        this.blocks.add(new RobotEnemyBlock(new Location(250, platformY - enemyHeightOffset), 500, 180));
        this.blocks.add(new RobotEnemyBlock(new Location(700, platformY - enemyHeightOffset), 500, 180));
        this.blocks.add(new RobotEnemyBlock(new Location(1050, platformY - enemyHeightOffset), 500, 180));
        this.blocks.add(new RobotEnemyBlock(new Location(1400, platformY - enemyHeightOffset), 500, 180));

        // Platziert den Zielblock am Ende
        blocks.add(new FinishBlock(new Location(1800, height - 390)));

        // Zeichne Plattformen
        for (Platform platform : this.platforms) {
            platform.drawPlatform();
        }

        // Zeichne Blöcke
        for (Block block : this.blocks) {
            block.draw(root);
        }
    }

    /**
     * Aktualisiert die Level-Logik.
     * Derzeit leer, da die Gegner-Logik wahrscheinlich in den Blöcken selbst implementiert ist.
     */
    @Override
    public void update() {
        // Logik zur Steuerung des Boss-Levels
    }

    /**
     * Gibt das nächste Level zurück. Da dies das Boss-Level ist, gibt es normalerweise kein nächstes Level.
     *
     * @return {@code null}, da dies das Ende des Spiels darstellt.
     */
    @Override
    public Level getNextLevel() {
        return null;
    }
}