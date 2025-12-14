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

/**
 * Die abstrakte Klasse {@code Level} dient als Basis für alle Spielabschnitte im Spiel.
 * <p>
 * Sie verwaltet die statischen Geometrieelemente eines Levels (Plattformen und Blöcke)
 * und bietet grundlegende Funktionen wie die Initialisierung, die Verwaltung von Übergängen
 * zum nächsten Level sowie die Platzierung von Lava in Lücken zwischen Plattformen.
 *
 * @author Tom Coombs
 * @author leonardo (aka. Phantomic)
 *
 * @see Platform
 * @see Block
 */
@Getter
public abstract class Level {
    /**
     * Der Name des Levels (z. B. "Level 1", "Einführung").
     */
    protected String name;

    /**
     * Liste der statischen Plattformen, auf denen sich der Spieler bewegen kann.
     */
    protected final List<Platform> platforms;

    /**
     * Liste der interaktiven Blöcke im Level (z. B. Lava, Hindernisse).
     */
    protected final List<Block> blocks;

    /**
     * Der Zeitpunkt (in Millisekunden), zu dem das Level gestartet wurde.
     */
    private long levelStarted;

    /**
     * Das nächste Level, zu dem nach Abschluss gewechselt werden soll.
     */
    @Getter
    private Level nextLevel;

    /**
     * Konstruiert eine neue Level-Instanz mit dem angegebenen Namen.
     *
     * @param name Der Name des Levels.
     */
    public Level(String name) {
        this.name = name;

        this.platforms = new ArrayList<>();
        this.blocks =  new ArrayList<>();
        this.levelStarted = System.currentTimeMillis();
    }

    /**
     * Wird aufgerufen, wenn das Level erfolgreich abgeschlossen wurde.
     * Kann von Unterklassen überschrieben werden, um spezifische Aktionen auszuführen.
     */
    public void onFinish() { }

    /**
     * Abstrakte Methode zum Zeichnen und Hinzufügen aller Level-Elemente (Plattformen, Blöcke) zum Root-Pane.
     *
     * @param width Die Breite des Anzeigebereichs.
     * @param height Die Höhe des Anzeigebereichs.
     * @param root Das Root-Pane, in das die Elemente gezeichnet werden.
     */
    public abstract void draw(double width, double height, Pane root);

    /**
     * Abstrakte Methode zur Aktualisierung der Level-Logik (z. B. Bewegung von Elementen, Zustandsprüfungen).
     */
    public abstract void update();

    /**
     * Fügt einen Lava-Block (Block) in jede horizontale Lücke zwischen aufeinanderfolgenden Plattformen ein.
     * Es wird davon ausgegangen, dass 'this.platforms' eine Liste von Platform-Objekten und
     * 'this.blocks' eine Liste von Block-Objekten ist.
     * Der Lava-Block wird an einer festen vertikalen Position platziert.
     *
     *
     * @param sceneHeight Die Gesamthöhe der Spielszene, oft zur Definition des Bodens verwendet.
     */
    protected void placeLavaBetweenPlatforms(double sceneHeight) {
        final double LAVA_TOP_Y = sceneHeight - 70.0;
        final double LAVA_HEIGHT = 250.0;

        List<Platform> orderedPlatforms = new ArrayList<>(this.platforms);

        // Plattformen nach ihrer X-Position sortieren, um die Lücken korrekt zu identifizieren
        orderedPlatforms.sort(Comparator.comparingDouble(Platform::getX));

        if (orderedPlatforms.size() < 2) {
            return;
        }

        for (int i = 0; i < orderedPlatforms.size() - 1; i++) {
            Platform currentPlatform = orderedPlatforms.get(i);
            Platform nextPlatform = orderedPlatforms.get(i + 1);

            // Startpunkt der Lücke: Ende der aktuellen Plattform
            double gapStart = currentPlatform.getX() + currentPlatform.getWidth();

            // Breite der Lücke: Start der nächsten Plattform minus Ende der aktuellen
            double gapWidth = nextPlatform.getX() - gapStart;

            // Nur Blöcke platzieren, wenn die Lücke signifikant ist
            if (gapWidth > 1.0) {
                this.blocks.add(createLavaColumn(gapStart, LAVA_TOP_Y, gapWidth, LAVA_HEIGHT));
            }
        }
    }

    /**
     * Hilfsmethode zur Erstellung eines {@code LavaBlock}-Objekts mit den angegebenen Abmessungen.
     *
     * @param x Die X-Koordinate der Lava-Spalte (linke obere Ecke).
     * @param y Die Y-Koordinate der Lava-Spalte (linke obere Ecke).
     * @param width Die Breite der Lava-Spalte.
     * @param height Die Höhe der Lava-Spalte.
     * @return Eine konfigurierte {@code LavaBlock}-Instanz.
     */
    protected static LavaBlock createLavaColumn(double x, double y, double width, double height) {
        LavaBlock lava = new LavaBlock(new Location(x, y));
        lava.setWidth(width);
        lava.setHeight(height);
        return lava;
    }
}