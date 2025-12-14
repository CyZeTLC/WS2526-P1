package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.LaserBlock;
import de.cyzetlc.hsbi.game.gui.block.impl.GasBarrierBlock;
import de.cyzetlc.hsbi.game.gui.block.impl.RobotEnemyBlock;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Direction;
import de.cyzetlc.hsbi.message.MessageHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Klasse {@code GameScreen} repräsentiert den zentralen In-Game-Zustand und ist sowohl
 * für die visuelle Darstellung als auch für die Kernlogik des laufenden Spiels verantwortlich.
 *
 * <p>
 * Diese Klasse fungiert als Haupt-Controller für die Gameplay-Sitzung und übernimmt folgende Aufgaben:
 * <ul>
 * <li><b>Rendering:</b> Zeichnet den Spieler, Level-Elemente (Blöcke, Plattformen), animierte Hintergründe und das HUD (Heads-Up Display).</li>
 * <li><b>Physik-Engine:</b> Berechnet Schwerkraft, Bewegungsgeschwindigkeit und Kollisionserkennung gegen Plattformen, Barrieren und Feinde.</li>
 * <li><b>Kamera-Steuerung:</b> Verwaltet die Viewport-Koordinaten (CameraX/Y) mit einem sanften Scroll-Algorithmus, um den Spieler zu verfolgen.</li>
 * <li><b>Eingabeverarbeitung:</b> Verarbeitet Benutzereingaben für Bewegung (WASD), Springen (Leertaste), Interaktion (E) und UI-Steuerungen.</li>
 * <li><b>Spielzustandsverwaltung:</b> Behandelt Gewinn-/Verlustbedingungen (Game Over), Pausenlogik und Szenenübergänge.</li>
 * </ul>
 * <p>
 * Zusätzlich implementiert diese Klasse umfangreiche Debugging-Tools, die über Funktionstasten zugänglich sind:
 * <ul>
 * <li><b>F1:</b> Schaltet Tooltips, Questfortschritt und Hilfetexte um.</li>
 * <li><b>F2:</b> Schaltet die technische Debug-Leiste (FPS, Koordinaten, etc.) um.</li>
 * <li><b>F3:</b> Schaltet "NoClip" und "GodMode" (Fliegen und Unverwundbarkeit) um.</li>
 * </ul>
 *
 * @author Tom Coombs
 * @author Leonardo (aka. Phantomic)
 *
 * @see GuiScreen
 * @see EntityPlayer
 * @see ScreenManager
 */
public class GameScreen implements GuiScreen {
    /**
     * Der Wurzel-Container für alle auf dem Bildschirm angezeigten visuellen Elemente.
     */
    protected final Pane root = new Pane();

    /**
     * Referenz auf den ScreenManager, der für die Behandlung von Bildschirmübergängen (z. B. Pause, Game Over) verwendet wird.
     */
    protected final ScreenManager screenManager;

    /**
     * Die Haupt-Spieler-Entität.
     */
    private EntityPlayer player;

    /**
     * Aktuelle Geschwindigkeitskomponente in X-Richtung (horizontale Bewegung).
     */
    private double dx = 1;

    /**
     * Aktuelle Geschwindigkeitskomponente in Y-Richtung (vertikale Bewegung/Schwerkraft).
     */
    private double dy = 0.5;

    /**
     * Text-Label zur Anzeige von Debug-Informationen (z. B. FPS, Koordinaten).
     */
    private Text debugLbl;

    /**
     * Flag, das anzeigt, ob das Spiel derzeit pausiert ist.
     */
    private boolean paused = false;

    /**
     * Das durchscheinende Overlay-Pane, das angezeigt wird, wenn das Spiel pausiert ist.
     */
    private Pane pauseOverlay;

    /**
     * Flag, das sicherstellt, dass die Game-Over-Sequenz nur einmal ausgelöst wird, um
     * mehrfache Bildschirmübergänge zu verhindern.
     */
    private boolean gameOverTriggered = false;

    /**
     * Die aktuelle X-Koordinate der Kamera/des Viewports in der Spielwelt.
     * Dieser Wert bestimmt den horizontalen Versatz für das Rendern von Spielelementen.
     */
    @Getter
    private double cameraX = 0;

    /**
     * Die aktuelle Y-Koordinate der Kamera/des Viewports in der Spielwelt.
     * Dieser Wert bestimmt den vertikalen Versatz für das Rendern von Spielelementen.
     */
    @Getter
    private double cameraY = 0;

    /**
     * Glättungsfaktor (Interpolationswert), der verwendet wird, um die Kamera allmählich
     * zur Zielposition zu bewegen, wodurch ein sanfter Verfolgungseffekt entsteht. (0.0 bis 1.0)
     */
    private final double cameraSmooth = 0.1; // wie schnell die Kamera folgt

    /**
     * Horizontaler Rand (Todeszone) in Pixeln. Die Kamera beginnt erst mit der Verfolgung des Spielers,
     * wenn dieser sich außerhalb dieses Randes bewegt.
     */
    private final double marginX = 400;

    /**
     * Vertikaler Rand (Todeszone) in Pixeln. Die Kamera beginnt erst mit der Verfolgung des Spielers,
     * wenn dieser sich außerhalb dieses Randes bewegt.
     */
    private final double marginY = 150;

    /**
     * Text-Label zur Anzeige von Hinweisen bezüglich des Flipper-Items und der Interaktion (KeyCode.E).
     */
    private Text flipperHint;

    /**
     * Flag, um zu verfolgen, ob der Flipper-Interaktionshinweis dem Spieler bereits angezeigt wurde.
     */
    private boolean flipperHintShown = false;

    /**
     * Text-Label, das das aktuelle Haupt-Questziel anzeigt.
     */
    private Text questLbl;

    /**
     * Text-Label, das den Fortschritt des Spielers beim Sammeln von Dateien anzeigt (z. B. "Files: 3/5").
     */
    private Text filesProgressLbl;

    /**
     * Die Gesamtzahl der einsammelbaren FolderBlock-Items, die im aktuellen Level vorhanden sind.
     */
    private int totalFolderCount = 0;

    /**
     * Umschaltzustand für die Anzeige von benutzerorientierten Tooltips und Questinformationen (gesteuert durch F1).
     */
    @Getter @Setter
    private boolean showTooltips = true; // F1

    /**
     * Text-Label zur Anzeige allgemeiner Steuerungshinweise (z. B. F1/F2/F3-Anweisungen).
     */
    private Text tipsLbl;

    /**
     * Eine Liste von ImageView-Objekten, die die Herz-Symbole (voll, halb, leer) darstellen,
     * welche zur visuellen Anzeige der Gesundheit des Spielers im HUD verwendet werden.
     */
    private List<ImageView> heartImageViews;

    /**
     * Konstruiert einen neuen GameScreen.
     *
     * @param screenManager Die ScreenManager-Instanz, die für die Behandlung von Bildschirmübergängen verantwortlich ist.
     */
    public GameScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.heartImageViews = new ArrayList<>();
    }

    /**
     * Initialisiert den GameScreen und richtet die gesamte Szene ein.
     *
     * <p>
     * Diese Methode wird einmal beim Wechsel zu diesem Bildschirm aufgerufen und führt alle notwendigen
     * anfänglichen Einrichtungsarbeiten der grafischen Oberfläche, des Spielzustands und der Steuerung durch.
     * Die Schritte umfassen:
     * <ul>
     * <li>Löschen aller vorherigen Elemente aus dem Wurzel-Pane.</li>
     * <li>Zeichnen des animierten Hintergrunds.</li>
     * <li>Setzen des Spielers an der Startposition und Sicherstellen, dass die Gesundheitspunkte korrekt initialisiert sind.</li>
     * <li>Löschen und erneutes Laden aller {@code Blocks} und {@code Platforms} des aktuellen Levels.</li>
     * <li>Zeichnen statischer HUD-Elemente, wie z. B. der "Zurück"- und "Pause"-Schaltflächen, Debug-Textfelder und der Gesundheitsanzeige.</li>
     * <li>Einrichten des Pause-Overlays und Konfigurieren der anfänglichen HUD-Layout-Positionen.</li>
     * </ul>
     * Diese Methode stellt sicher, dass der Bildschirm für die logische Verarbeitung innerhalb des {@code update}-Zyklus bereit ist.
     *
     * @see GameScreen#update(double)
     * @see de.cyzetlc.hsbi.game.level.Level#draw(double, double, javafx.scene.layout.Pane)
     */
    @Override
    public void initialize() {
        root.getChildren().clear();

        MessageHandler messageHandler = Game.getInstance().getMessageHandler();
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();
        this.player = Game.thePlayer;

        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");

        if (player.getHealth() <= 0) {
            player.setHealth(player.getMaxHealth());
        }
        player.drawPlayer(root, 20 - cameraX,
                height - 450 - cameraY);

        /*
         * Entfernt alle Blöcke & Plattformen aus dem aktuellen Level und
         * lädt das aktuelle Level neu
         */
        Game.getInstance().getCurrentLevel().getBlocks().clear();
        Game.getInstance().getCurrentLevel().getPlatforms().clear();
        Game.getInstance().getCurrentLevel().draw(width, height, root);

        // Zeichnet die beiden Schaltflächen oben links
        UIUtils.drawButton(root, messageHandler.getMessageForLanguage("gui.game.btn.back"), 10, 10, () -> screenManager.showScreen(new MainMenuScreen(screenManager)));
        //UIUtils.drawButton(root, "Pause", 150, 10, this::togglePause);

        this.debugLbl = UIUtils.drawText(root, "NaN", 10, 85);
        this.debugLbl.setVisible(true); // alte Debug-Anzeige verstecken, ersetzen wir durch debugBarLbl
        this.flipperHint = UIUtils.drawText(root, "", 10, 135);
        this.flipperHint.setVisible(false);

        this.totalFolderCount = this.countFolderBlocks();
        this.questLbl = UIUtils.drawText(root, messageHandler.getMessageForLanguage("gui.game.lbl.quest"), 10, 0);
        this.filesProgressLbl = UIUtils.drawText(root, "", 10, 0);
        this.updateFolderProgress();

        // HUD-Hinweise
        this.tipsLbl = UIUtils.drawText(root, messageHandler.getMessageForLanguage("gui.game.lbl.tips"), 10, 0);
        this.tipsLbl.setVisible(showTooltips);

        this.layoutHudPositions();
        this.setupPauseOverlay(width, height);
        this.createHealthBar(width);
    }

    /**
     * Führt die Haupt-Spielschleifenlogik aus, die einmal pro Frame läuft, um den Spielzustand zu aktualisieren.
     * <p>
     * Diese Methode ist für alle dynamischen Spielelemente verantwortlich, einschließlich Physik, Eingabeverarbeitung,
     * Kollisionsauflösung, Kamerabewegung und HUD-Updates.
     * <p>
     * Zu den wichtigsten Vorgängen, die in dieser Methode ausgeführt werden, gehören:
     * <ul>
     * <li>Verarbeitung der von der Bildrate unabhängigen Physik (Schwerkraft, Geschwindigkeitsberechnung).</li>
     * <li>Verarbeitung der Standard-Spielereingaben für Bewegung, Springen und Interaktion (E).</li>
     * <li>Verwaltung der Debug-Eingabeumschaltungen (F1) für Tooltips.</li>
     * <li>Umfassende Kollisionserkennung gegen {@code Platforms} und verschiedene Arten von {@code Blocks}.</li>
     * <li>Verarbeitung spezifischer Interaktionen, wie das Einsammeln von Gegenständen (Flipper), das Aktivieren von Barrieren (GasBarrier) und Feindkampf (RobotEnemyBlock).</li>
     * <li>Aktualisierung der Kameraposition basierend auf der Spielerposition für sanftes Scrollen.</li>
     * <li>Prüfung auf Game-Over-Bedingungen (Verlust der Gesundheit oder Herausfallen aus der Welt).</li>
     * <li>Aktualisierung aller dynamischen HUD-Elemente (Gesundheitsprozentsatz, Ordnerfortschritt, Debug-Status).</li>
     * </ul>
     *
     * @param delta Die seit dem letzten Frame verstrichene Zeit, die verwendet wird, um sicherzustellen, dass die Physikberechnungen
     * von der Bildrate unabhängig sind.
     * @see GameScreen#initialize()
     * @see GameScreen#updateCamera(double, double)
     * @see de.cyzetlc.hsbi.game.entity.EntityPlayer
     */
    @Override
    public void update(double delta) {
        if (player.getHealth() <= 0) {
            this.handleGameOver();
            return;
        }

        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        double gravity = Game.gravity;       // Schwerkraftstärke
        double moveSpeed = Game.moveSpeed;    // horizontale Geschwindigkeit
        double jumpPower = Game.jumpPower;    // Sprungkraft
        boolean onGround = false;
        boolean hittingCeiling = false;
        boolean interactPressed = screenManager.getInputManager().isPressed(KeyCode.E);
        boolean f1 = screenManager.getInputManager().pollJustPressed(KeyCode.F1);

        // Tooltips toggeln
        tipsLbl.setVisible(showTooltips);
        questLbl.setVisible(showTooltips);
        filesProgressLbl.setVisible(showTooltips);

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();

        // Eingabe
        if (screenManager.getInputManager().isPressed(KeyCode.A)) {
            dx = -moveSpeed * delta;
            player.setDirection(Direction.WALK_LEFT);
        } else if (screenManager.getInputManager().isPressed(KeyCode.D)) {
            dx = moveSpeed * delta;
            player.setDirection(Direction.WALK_RIGHT);
        } else {
            dx = 0;
        }

        // Schwerkraft
        dy += gravity * delta;

        // Versuchte Position
        double nextX = x + dx;
        double nextY = y + dy;

        Rectangle2D nextBounds = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());

        // Plattform-Kollisionen
        for (Platform platform : Game.getInstance().getCurrentLevel().getPlatforms()) {
            Rectangle2D pBounds = platform.getBounds();
            platform.update(this);

            if (nextBounds.intersects(pBounds)) {
                // Landung von oben
                if (y + player.getHeight() <= platform.getY()) {
                    nextY = platform.getY() - player.getHeight();
                    dy = 0;
                    onGround = true;
                }
                // Kollision von unten
                if (dy < 0 && y >= platform.getY() + platform.getHeight() && nextY <= platform.getY() + platform.getHeight()) {
                    nextY = platform.getY() + platform.getHeight();
                    hittingCeiling = true;
                    dy = 0;
                } else {
                    hittingCeiling = false;
                }

                // Seitenkollision
                if (x + player.getWidth() <= platform.getX()) {
                    nextX = platform.getX() - player.getWidth();
                    dx = 0;
                }
                else if (x >= platform.getX() + platform.getWidth()) {
                    nextX = platform.getX() + platform.getWidth();
                    dx = 0;
                }
            }
        }

        // Block-Kollisionen
        List<Block> pendingBlocks = new ArrayList<>();
        List<Block> blocks = Game.getInstance().getCurrentLevel().getBlocks();
        for (Block block : blocks) {
            Rectangle2D pBounds = block.getBounds();

            // Flipper-Item-Logik: Einsammeln & HUD-Flag setzen
            if (block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FlipperItem flipperItem) {
                flipperItem.update(player);
            } else {
                block.update();
            }

            if (block instanceof RobotEnemyBlock enemy) {
                // Feind-Schusslogik
                LaserBlock laser = enemy.tryFire(player);
                if (laser != null) {
                    laser.draw(root);
                    pendingBlocks.add(laser);
                }
            }

            if (nextBounds.intersects(pBounds) && block.isActive() && !player.isNoClipEnabled()) {
                if (block instanceof GasBarrierBlock barrier && interactPressed && player.hasFlipper()) {
                    // Gasbarriere deaktivieren, wenn Spieler Flipper hat und E drückt
                    barrier.deactivate();
                    continue;
                }
                if (block instanceof RobotEnemyBlock enemy) {
                    double enemyTop = enemy.getLocation().getY();
                    // Prüfen, ob der Spieler auf den Feind tritt
                    boolean stomp = (y + player.getHeight() <= enemyTop + 6) && dy > 0;
                    if (stomp) {
                        enemy.kill();
                        nextY = enemyTop - player.getHeight();
                        dy = -jumpPower * delta * 0.6; // leichten Sprung-Rebound geben
                    } else {
                        enemy.hitPlayer(player); // Spieler nimmt Schaden
                    }
                    // Mit Kollisionsauflösung fortfahren, aber doppelte onCollide überspringen
                } else {
                    block.onCollide(player);
                }

                if (block.isCollideAble()) {
                    // Landung von oben
                    if (y + player.getHeight() <= block.getLocation().getY()) {
                        nextY = block.getLocation().getY() - player.getHeight();
                        nextX += block.getDeltaX(); // der Bewegung einer beweglichen Plattform folgen
                        dy = 0;
                        onGround = true;
                    }
                    // Links
                    else if (x + player.getWidth() <= block.getLocation().getX()) {
                        nextX = block.getLocation().getX() - player.getWidth();
                        dx = 0;
                    }
                    // Rechts
                    else if (x >= block.getLocation().getX() + block.getWidth()) {
                        nextX = block.getLocation().getX() + block.getWidth();
                        dx = 0;
                    }
                }
            }
        }
        if (!pendingBlocks.isEmpty()) {
            // Neue Projektile zur Blockliste hinzufügen
            blocks.addAll(pendingBlocks);
        }

        // Fensterbegrenzung
        if (nextX < 0) nextX = 0;

        // Herausfallen aus der Welt -> Game Over
        double screenNextY = nextY - this.cameraY;
        if (screenNextY + player.getHeight() > height) {
            if (player.getHealth() > 0) {
                player.setHealth(0);
            }
            this.handleGameOver();
            return;
        }

        // Springen (nur wenn auf dem Boden)
        if (screenManager.getInputManager().isPressed(KeyCode.SPACE) && dy == 0 && !hittingCeiling) {
            dy = -jumpPower * delta;
            player.setDirection(Direction.JUMP);
        }

        // Position anwenden
        player.getLocation().setX(nextX);
        player.getLocation().setY(nextY);

        this.updateCamera(width, height);
        this.updateDebugBar(onGround, moveSpeed, jumpPower);
        this.updateFolderProgress();
        this.updateHealth();

        if (player.hasFlipper() && !flipperHintShown) {
            flipperHint.setText("Druecke E damit der Flipper mit Gegenstaenden interagiert");
            flipperHint.setVisible(true);
            flipperHintShown = true;
        }

        player.update();
    }

    /**
     * Aktualisiert die Position des Kamera-Viewports, um dem Spieler mit einem sanften Scroll-Effekt zu folgen.
     *
     * <p>
     * Diese Methode implementiert eine "Todeszonen"-Logik: Die Kamera beginnt sich erst zu bewegen, wenn die
     * Bildschirmposition des Spielers die vordefinierten Ränder ({@code marginX} und {@code marginY}) überschreitet.
     * Die Bewegung wird dann unter Verwendung des {@code cameraSmooth}-Faktors geglättet, um ruckartige Übergänge zu vermeiden.
     * <p>
     * Die Kamera wird auch begrenzt, um das Anzeigen von Koordinaten kleiner als Null zu verhindern, wodurch
     * sichergestellt wird, dass der Viewport innerhalb der beabsichtigten Level-Grenzen bleibt, es sei denn, der NoClip-Modus ist aktiv.
     *
     * @param width  Die Breite des Bildschirms/der Bühne, die zur Berechnung der rechten Randgrenze verwendet wird.
     * @param height Die Höhe des Bildschirms/der Bühne, die zur Berechnung der unteren Randgrenze verwendet wird.
     * @see GameScreen#cameraSmooth
     * @see EntityPlayer#isNoClipEnabled()
     */
    private void updateCamera(double width, double height) {
        double playerScreenX = player.getLocation().getX() - this.cameraX;
        double playerScreenY = player.getLocation().getY() - this.cameraY;

        double targetCamX = this.cameraX;
        double targetCamY = this.cameraY;

        if (playerScreenX > width - this.marginX) {
            targetCamX += playerScreenX - (width - this.marginX);
        } else if (playerScreenX < this.marginX) {
            targetCamX -= (this.marginX - playerScreenX);
        }

        if (playerScreenY > height - this.marginY) {
            targetCamY += playerScreenY - (height - this.marginY);
        } else if (playerScreenY < this.marginY) {
            targetCamY -= (this.marginY - playerScreenY);
        }

        // sanftes Folgen (definiert durch cameraSmooth)
        this.cameraX += (targetCamX - this.cameraX) * this.cameraSmooth;
        this.cameraY += (targetCamY - this.cameraY) * this.cameraSmooth;

        // Kamera-Clamp nur im Normalmodus; im NoClip darf man frei fliegen
        if (!player.isNoClipEnabled()) {
            if (this.cameraX < 0) this.cameraX = 0;
            if (this.cameraY < 0) this.cameraY = 0;
        }
    }

    /**
     * Erstellt und initialisiert das visuelle Overlay, das angezeigt wird, wenn das Spiel pausiert ist.
     * Es enthält einen transparenten schwarzen Hintergrund, einen "Pause"-Titel und Steuerschaltflächen
     * (Weiter und Zurück zum Menü).
     *
     * @param width Die aktuelle Breite des Spielfensters/der Bühne.
     * @param height Die aktuelle Höhe des Spielfensters/der Bühne.
     */
    private void setupPauseOverlay(double width, double height) {
        this.pauseOverlay = new Pane();
        UIUtils.drawRect(pauseOverlay, 0, 0, width, height, Color.BLACK).setOpacity(0.5);
        Text title = UIUtils.drawCenteredText(pauseOverlay, "Pause", 0, height / 2 - 120, false);
        title.setFill(Color.WHITE);

        UIUtils.drawCenteredButton(pauseOverlay, "Weiter", 0, height / 2 - 50, false, this::togglePause);
        UIUtils.drawCenteredButton(pauseOverlay, "Zum Menu", 0, height / 2 + 20, false, () -> {
            this.paused = false;
            this.pauseOverlay.setVisible(false);
            screenManager.showScreen(new MainMenuScreen(screenManager));
        });

        this.pauseOverlay.setVisible(false);
        root.getChildren().add(pauseOverlay);
    }

    /**
     * Schaltet den Pausenzustand des Spiels um.
     * Wenn das Spiel pausiert ist, wird es fortgesetzt und das Pause-Overlay wird ausgeblendet.
     * Wenn das Spiel läuft, wird es pausiert und das Pause-Overlay wird angezeigt.
     */
    private void togglePause() {
        this.paused = !this.paused;
        if (this.pauseOverlay != null) {
            this.pauseOverlay.setVisible(this.paused);
        }
    }

    /**
     * Behandelt den Game-Over-Zustand.
     * Dies wird ausgelöst, wenn die Gesundheit des Spielers auf Null sinkt oder er aus der Welt fällt.
     * Es verhindert mehrere Auslösungen und wechselt den Bildschirm zurück zum Hauptmenü.
     */
    private void handleGameOver() {
        if (this.gameOverTriggered) {
            return;
        }
        this.gameOverTriggered = true;
        /*if (!(screenManager.getCurrentScreen() instanceof MainMenuScreen)) {
            this.screenManager.showScreen(new MainMenuScreen(screenManager));
        }*/
    }

    /**
     * Aktualisiert die Bilder (Source-URL) der bereits erstellten ImageView-Objekte
     * basierend auf dem aktuellen Gesundheitswert des Spielers.
     */
    private void updateHealth() {
        double lives = player.getHealth();
        int maxLives = (int) player.getMaxHealth();

        for (int i = 0; i < maxLives; i++) {
            if (i >= this.heartImageViews.size()) {
                break;
            }

            ImageView heart = this.heartImageViews.get(i);
            String imagePath;

            if (lives >= i + 1) {
                imagePath = "/assets/hud/heart_full.png";
            } else if (lives > i && lives < i + 1) {
                imagePath = "/assets/hud/heart_half.png";
            } else {
                imagePath = "/assets/hud/heart_empty.png";
            }

            heart.setImage(new Image(imagePath));
        }
    }

    /**
     * Zeichnet die Gesundheitsleiste des Spielers visuell mithilfe von Herz-Symbolen (voll, halb, leer).
     * Die Gesundheitsanzeige ist an der oberen rechten Ecke des Bildschirms ausgerichtet.
     *
     * @param width Die aktuelle Breite des Spielfensters/der Bühne, die für die Ausrichtung auf der rechten Seite verwendet wird.
     */
    private void createHealthBar(double width) {
        int heartSize = 32;
        int padding = 4;
        int maxLives = (int) player.getMaxHealth();

        int startX = (int) (width - (maxLives * (heartSize + padding)) - 16);

        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * (heartSize + padding);

            ImageView heart = new ImageView("/assets/hud/heart_empty.png");
            heart.setFitWidth(heartSize);
            heart.setFitHeight(heartSize);
            heart.setLayoutX(x);
            heart.setLayoutY(16);

            root.getChildren().add(heart);
            this.heartImageViews.add(heart);
        }
    }

    /**
     * Legt die konsistenten vertikalen Positionen für alle Heads-Up Display (HUD)-Text-Elemente fest.
     * Diese Methode stellt sicher, dass Textblöcke wie Debug-Informationen, Tipps und Questfortschritt
     * ordentlich unterhalb der primären Steuerschaltflächen (Zurück, Pause) angeordnet sind.
     */
    private void layoutHudPositions() {
        int hudX = 0;
        int hudStartY = 140;       // Start unter den Buttons
        int hudLineHeight = 18;    // Zeilenabstand innerhalb eines Blocks
        int hudBlockSpacing = 10;  // Abstand zwischen Blöcken
        int y = hudStartY;


        // Block 2: Status + Debug-State + Tooltips (entzerrt)
        if (this.tipsLbl != null) {
            this.tipsLbl.setX(hudX);
            this.tipsLbl.setY(y);
            y += hudLineHeight + 4; // zusätzlicher Abstand
        }
        y += hudBlockSpacing * 2; // größerer Abstand vor dem Quest-Block

        // Block 3: Quest + Files
        if (this.questLbl != null) {
            this.questLbl.setX(hudX);
            this.questLbl.setY(y);
            y += hudLineHeight;
        }
        if (this.filesProgressLbl != null) {
            y += 6; // Files etwas weiter unter die Quest schieben
            this.filesProgressLbl.setX(hudX);
            this.filesProgressLbl.setY(y);
            y += hudLineHeight;
        }
    }

    /**
     * Aktualisiert und formatiert die Debug-Informationen, die in der oberen linken Ecke angezeigt werden (Debug-Leiste).
     * Diese Leiste zeigt technische Details wie FPS, Spielerstatus, Kamerakoordinaten und Spielparameter.
     * Sie ist nur sichtbar, wenn das Flag {@code showDebugBar} auf true gesetzt ist (umgeschaltet durch F2).
     *
     * @param onGround Zeigt an, ob der Spieler sich gerade auf einer Oberfläche befindet.
     * @param moveSpeed Der aktuelle Parameter für die horizontale Bewegungsgeschwindigkeit.
     * @param jumpPower Der aktuelle Parameter für die vertikale Sprungkraft.
     */
    private void updateDebugBar(boolean onGround, double moveSpeed, double jumpPower) {
        String line1 = "FPS: " + (int) screenManager.getCurrentFps()
                + " | onGround: " + onGround
                + " | moveSpeed: " + moveSpeed
                + " | jumpPower: " + jumpPower
                + " | Location: " + player.getLocation();
        String line2 = "cameraX: " + (int) cameraX
                + " | cameraY: " + (int) cameraY
                + " | Location: " + player.getLocation().toString();
        String line3 = "HP: " + (int) Math.round(player.getHealth() / player.getMaxHealth() * 100.0)
                + " | NoClip: " + player.isNoClipEnabled()
                + " | God: " + player.isGodModeEnabled();
        this.debugLbl.setText(line1);
    }

    /**
     * Zählt die Gesamtzahl der Ordnerblöcke (einsammelbare Dateien), die im aktuellen Level vorhanden sind.
     *
     * @return Die Gesamtzahl der {@code FolderBlock}-Instanzen in der Blockliste des Levels.
     */
    private int countFolderBlocks() {
        return (int) Game.getInstance().getCurrentLevel().getBlocks().stream()
                .filter(block -> block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FolderBlock)
                .count();
    }

    /**
     * Zählt die Anzahl der aktiven (nicht eingesammelten) Ordnerblöcke, die im aktuellen Level verbleiben.
     *
     * @return Die Anzahl der {@code FolderBlock}-Instanzen, die derzeit aktiv sind.
     */
    private int countActiveFolders() {
        return (int) Game.getInstance().getCurrentLevel().getBlocks().stream()
                .filter(block -> block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FolderBlock)
                .filter(de.cyzetlc.hsbi.game.gui.block.Block::isActive)
                .count();
    }

    /**
     * Aktualisiert das HUD-Element, das den Fortschritt beim Sammeln von Dateien anzeigt.
     * Es berechnet die Anzahl der gesammelten Dateien im Vergleich zur Gesamtzahl der verfügbaren Dateien
     * und setzt den Text für {@code filesProgressLbl}.
     */
    private void updateFolderProgress() {
        if (this.totalFolderCount == 0) {
            this.filesProgressLbl.setText("Files: 0/0");
            return;
        }
        int active = countActiveFolders();
        int collected = Math.max(0, this.totalFolderCount - active);
        this.filesProgressLbl.setText("Files: " + collected + "/" + this.totalFolderCount);
    }

    /**
     * Ruft das Wurzel-Pane des GameScreens ab, das alle visuellen Elemente enthält.
     *
     * @return Das JavaFX {@code Pane}, das als Wurzel-Container verwendet wird.
     */
    @Override
    public Pane getRoot() {
        return root;
    }

    /**
     * Gibt den identifizierenden Namen dieses Bildschirms zurück.
     *
     * @return Der konstante Bildschirmname "GameScreen".
     */
    @Override
    public String getName() {
        return "GameScreen";
    }
}