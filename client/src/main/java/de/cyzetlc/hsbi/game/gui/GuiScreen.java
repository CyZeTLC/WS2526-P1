package de.cyzetlc.hsbi.game.gui;

import javafx.scene.layout.Pane;

/**
 * Das {@code GuiScreen} Interface definiert den Vertrag für alle Bildschirme innerhalb der grafischen Benutzeroberfläche (GUI) des Spiels.
 *
 * <p>
 * Jede Klasse, die einen Zustand repräsentiert (z. B. Hauptmenü, Spielbildschirm, Einstellungen), muss dieses Interface implementieren
 * und ein Root-Pane zur Anzeige sowie Methoden zur Initialisierung und Zustandsaktualisierung bereitstellen.
 *
 * @author Tom Coombs
 */
public interface GuiScreen {
    /**
     * Ruft das Root-Pane des GameScreens ab, das alle visuellen Elemente enthält.
     * Alle Komponenten, die diesem Bildschirm zugeordnet sind, sind Kinder dieses Panes.
     *
     * @return Das JavaFX {@code Pane}, das als Root-Container verwendet wird.
     */
    Pane getRoot();

    /**
     * Gibt den identifizierenden Namen dieses Bildschirms zurück.
     *
     * @return Der konstante Bildschirmname (z. B. "MainMenuScreen", "GameScreen").
     */
    String getName();

    /**
     * Initialisiert den Zustand und die visuellen Komponenten des Bildschirms.
     * Diese Methode wird einmal aufgerufen, wenn der Bildschirm zum ersten Mal geladen oder aktiviert wird.
     * Sie wird typischerweise zum Laden von Ressourcen, zum Festlegen von Anfangspositionen und zum Zeichnen statischer UI-Elemente verwendet.
     */
    void initialize();

    /**
     * Aktualisiert den Zustand des Bildschirms. Diese Methode ist Teil der zentralen Spielschleife.
     * <p>
     * Sie wird verwendet, um Physik zu berechnen, Eingaben zu verarbeiten, Animationen zu aktualisieren und Zustandsänderungen zu überprüfen.
     * Sie ist als Standardmethode (default method) definiert, damit statische Bildschirme (wie einfache Menüs)
     * nicht gezwungen sind, komplexe Aktualisierungslogik zu implementieren.
     *
     * @param delta Die seit dem letzten Frame verstrichene Zeit, verwendet für Frame-Raten-unabhängige Berechnungen.
     */
    default void update(double delta) {}
}