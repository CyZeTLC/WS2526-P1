# 🎮 Dokumentation: Aufbau von GUI und Level-Architektur

In diesem Abschnitt wird der schrittweise Aufbau der **Graphical User Interface (GUI)** sowie die architektonische Umsetzung der **Levels** dokumentiert. Wichtige Design- und technische Entscheidungen werden kommentiert.

---

## 1. GUI-Rendering mit JavaFX

Die gesamte grafische Oberfläche des Spiels wird mithilfe der **JavaFX-Bibliothek** gerendert.

### 📝 Kurze Erklärung zu JavaFX

**JavaFX** ist eine Java-Bibliothek, die speziell für die Entwicklung von Desktop-Anwendungen und Rich Internet Applications (RIA) konzipiert wurde. Im Vergleich zu älteren Technologien wie Swing bietet JavaFX eine **modernere, hardwarebeschleunigte Oberfläche** und unterstützt **CSS** für einfaches Styling.

* **Technische Notiz:** JavaFX wurde gewählt, da es **leistungsstarkes 2D-Rendering** und eine klare Trennung von Logik und Darstellung (mittels FXML/CSS) ermöglicht, was die Entwicklung der Benutzeroberfläche vereinfacht und beschleunigt.

---

## 2. Das `GuiScreen`-Konzept

Um die **verschiedenen Zustände** und Ansichten des Fensters (z. B. Hauptmenü, Einstellungen, eigentliches Spiel) sauber voneinander trennen und effizient wechseln zu können, wurde das **`GuiScreen`-Konzept** implementiert.

### 💡 Designentscheidung: `GuiScreen`

Die Entscheidung für dieses konzeptionelle Framework ermöglicht eine klare **Trennung der Zuständigkeiten (Single Responsibility Principle)**. Jede Ansicht (z. B. das Hauptmenü) wird zu einer **eigenständigen Klasse** (`MainMenuScreen`), die nur für ihre spezifische Logik und Darstellung verantwortlich ist.

* **Struktur:** Jede `GuiScreen`-Instanz besitzt die folgenden zentralen Methoden:
    * `initialize()`: Wird einmalig beim Erstellen des Screens ausgeführt, um alle zu rendernden Komponenten erstmalig zu erstellen (z. B. Buttons, Textfelder).
    * `draw()`: Zeichnet die erstellten Komponenten auf den Bildschirm.
    * `update()`: Wird **jeden Frame** ausgeführt, um Logik wie Animationen, Eingabeverarbeitung oder Zustandsprüfungen darzustellen.

### 💻 Implementierung des Screen-Wechsels

Der Wechsel zwischen den `GuiScreen`s wird über einen zentralen **`ScreenManager`** gesteuert.

* **Verwendeter Prompt (Konzept):** *Implementiere eine statische Klasse `ScreenManager` mit der Methode `showScreen(GuiScreen screen)`, die den aktuellen Bildschirminhalt durch den neuen Screen ersetzt und dessen `initialize()`-Methode aufruft.*

```java
public class ScreenManager {
    private static GuiScreen currentScreen;
    // ..

    public void showScreen(GuiScreen screen) {
        // Setze aktuellen Screen
        this.currentScreen = screen;

        /*
         * Wenn screen noch nie geladen wurde -> screen erstmals laden
         */
        if (!this.screenList.contains(screen)) {
            screen.initialize();
        }

        // Screen in Liste hinzufügen und als Root setzen (anzeigen)
        this.screenList.add(screen);
        Platform.runLater(() -> stage.getScene().setRoot(screen.getRoot()));
    }

    //..
}
```

## 3. Level-Implementierung über den `GameScreen`

Für das eigentliche Spiel-Gameplay wurde die Architektur so gestaltet, dass nicht für jedes Level ein neuer `GuiScreen` erstellt wird, sondern ein **einheitlicher `GameScreen`** als Container dient.

### 💡 Designentscheidung: Einheitlicher `GameScreen`

Anstatt viele Level-spezifische Screens zu erstellen (z. B. `Level1Screen`, `Level2Screen`), fungiert der **`GameScreen`** als **zentrale Spiel-Umgebung**. Dies vereinfacht das **Laden und Entladen von Level-Daten** und stellt sicher, dass Elemente wie das **HUD (Head-Up Display)** oder die **Pausenfunktion** konsistent über alle Level hinweg funktionieren.

* **Workflow beim Levelstart:** Der `GameScreen` ist dafür verantwortlich, die **Level-Daten** zu laden, die **HUD-Elemente** zu instanziieren und die **Spiel-Loop** zu starten.

### 💻 Implementierung des Level-Ladens

Der `GameScreen` orchestriert das Laden des aktuellen Levels und der Benutzeroberfläche (HUD).

* **Verwendeter Prompt (Konzept):** *Erweitere den `GameScreen`, sodass er in seiner `initialize()`-Methode das `Level#draw(width, height, root)` aufruft und anschließend die `HUD` als Overlay über das Spiel-Rendering lädt.*

```java
public class GameScreen extends GuiScreen {
    // ...
    @Override
    public void initialize() {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        // .. HUD laden ..

        /*
         * Remove all blocks & platforms from current level and
         * load draw current level
         */
        Game.getInstance().getCurrentLevel().getBlocks().clear();
        Game.getInstance().getCurrentLevel().getPlatforms().clear();
        Game.getInstance().getCurrentLevel().draw(width, height, root);

        // ...
    }

    @Override
    public void update() {
        // .. Update der Spiellogik (Bewegung, Kollision) ..
        

        // .. Update der HUD (Animationen etc.) ..
    }
    // ...
}
```
