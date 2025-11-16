package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.FloatingPlatformBlock;
import de.cyzetlc.hsbi.game.gui.block.JumpBoostBlock;
import de.cyzetlc.hsbi.game.gui.block.LavaBlock;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Direction;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    private EntityPlayer player;
    private double dx = 1; // Bewegung in X-Richtung
    private double dy = 0.5; // Bewegung in Y-Richtung

    private final Text debugLbl;
    private final Text healthLbl;

    private final List<Platform> platforms = new ArrayList<>();

    private final List<Block> blocks = new ArrayList<>();

    private boolean gameOverTriggered = false;

    public GameScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawRect(root, 0,0,width,height, Color.LIGHTBLUE);
        player = Game.thePlayer;
        if (player.getHealth() <= 0) {
            player.setHealth(1.0F);
        }
        player.drawPlayer(root, 20, height-450);

        // Zurück zum Menü
        UIUtils.drawButton(root, "Zurück", 10, 10, () -> screenManager.showScreen(new MainMenuScreen(screenManager)));

        this.debugLbl = UIUtils.drawText(root, "FPS: " + screenManager.getCurrentFps(), 10, 85);
        this.healthLbl = UIUtils.drawText(root, "HP: 100%", 10, 105);

        //Bsp.: -> wird später geändert
        platforms.add(new Platform(0, height-300, 450, 300, root)); // x, y, width, height
        platforms.add(new Platform(500, height-350, 200, 350, root)); // x, y, width, height
        platforms.add(new Platform(780, height-300, 150, 300, root));

        this.blocks.add(new JumpBoostBlock(new Location(150, height-332)));

        // Schwebende Plattform verbindet die beiden groesseren Inseln im oberen Bereich
        this.blocks.add(new FloatingPlatformBlock(
                new Location(420, height - 420),
                new Location(720, height - 420),
                120
        ));


        // Bloecke zeichnen
        this.fillGapsWithLava(height);
        for (Block block : this.blocks) {
            block.draw(root);
        }
    }

    @Override
    public void update(double delta) {
        if (player.getHealth() <= 0) {
            this.handleGameOver();
            return;
        }

        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        // Bewege dynamische Bloecke (z.B. schwebende Plattform) bevor Kollisionsabfragen stattfinden
        for (Block block : this.blocks) {
            block.update();
        }

        double gravity = Game.gravity;       // Stärke der Schwerkraft
        double moveSpeed = Game.moveSpeed;    // horizontale Bewegungsgeschwindigkeit (Pixel/Sek)
        double jumpPower = Game.jumpPower;    // Sprungkraft
        boolean onGround = false;  // Flag: steht der Spieler auf dem Boden?

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();

        // Eingabe (wird später auch noch in einen Listener gesteckt)
        if (screenManager.getInputManager().isPressed(KeyCode.A)) {
            dx = -moveSpeed * delta;
            player.setDirection(Direction.WALK_LEFT);
        } else if (screenManager.getInputManager().isPressed(KeyCode.D)) {
            dx = moveSpeed * delta;
            player.setDirection(Direction.WALK_RIGHT);
        } else {
            dx = 0; // kein Gleiten mehr
        }

        // Sprung (nur wenn auf dem Boden)
        if (screenManager.getInputManager().isPressed(KeyCode.SPACE) && dy == 0) {
            dy = -jumpPower * delta;
            player.setDirection(Direction.JUMP);
        }

        // Schwerkraft
        dy += gravity * delta;

        // Vorläufige Position berechnen
        double nextX = x + dx;
        double nextY = y + dy;

        Rectangle2D nextBounds = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());

        // Kollision mit Plattformen
        for (Platform platform : platforms) {
            Rectangle2D pBounds = platform.getBounds();

            if (nextBounds.intersects(pBounds)) {
                // Kollision von oben (Spieler landet)
                if (y + player.getHeight() <= platform.getY()) {
                    nextY = platform.getY() - player.getHeight();
                    dy = 0;
                    onGround = true;

                    //Spieler muss resetet werden
                }
                // Seitenkollision
                else if (x + player.getWidth() <= platform.getX()) {
                    nextX = platform.getX() - player.getWidth();
                    dx = 0;
                }
                else if (x >= platform.getX() + platform.getWidth()) {
                    nextX = platform.getX() + platform.getWidth();
                    dx = 0;
                }
            }
        }

        // Kollision mit Bloecke
        for (Block block : this.blocks) {
            Rectangle2D pBounds = block.getBounds();

            if (nextBounds.intersects(pBounds) && block.isActive()) {
                block.onCollide(player);

                if (block.isCollideAble()) {
                    // Kollision von oben (Spieler landet)
                    if (y + player.getHeight() <= block.getLocation().getY()) {
                        nextY = block.getLocation().getY() - player.getHeight();
                        nextX += block.getDeltaX(); // Spieler folgt seitlicher Bewegung der Plattform
                        dy = 0;
                        onGround = true;

                        //Spieler muss resetet werden
                    }
                    // Seitenkollision
                    else if (x + player.getWidth() <= block.getLocation().getX()) {
                        nextX = block.getLocation().getX() - player.getWidth();
                        dx = 0;
                    }
                    else if (x >= block.getLocation().getX() + block.getWidth()) {
                        nextX = block.getLocation().getX() + block.getWidth();
                        dx = 0;
                    }
                }
            }
        }

        // Kollision mit Fensterrand (später dann halt für den Game-Over Screen oder so)
        if (nextX < 0) nextX = 0;
        if (nextX + player.getWidth() > width) nextX = width - player.getWidth();

        // Aus dem Spielfeld gefallen -> Spieler "stirbt" und Hauptmenü wird via Listener geöffnet
        if (nextY + player.getHeight() > height) {
            if (player.getHealth() > 0) {
                player.setHealth(0);
            }
            this.handleGameOver();
            return;
        }

        // Position aktualisieren
        player.getLocation().setX(nextX);
        player.getLocation().setY(nextY);

        // Debug-Info (später maybe per F3 oder so ein/aus)
        this.debugLbl.setText("FPS: " + (int) screenManager.getCurrentFps() +
                " | onGround: " + onGround +
                " | moveSpeed: " + moveSpeed +
                " | jumpPower: " + jumpPower +
                " | uuid: " + player.getUuid());
        int hpPercent = (int) Math.round(player.getHealth() / player.getMaxHealth() * 100.0);
        this.healthLbl.setText("HP: " + hpPercent + "%");

        player.update();
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "GameScreen";
    }

    private void handleGameOver() {
        if (this.gameOverTriggered) {
            return;
        }
        this.gameOverTriggered = true;
        if (!(screenManager.getCurrentScreen() instanceof MainMenuScreen)) {
            this.screenManager.showScreen(new MainMenuScreen(screenManager));
        }
    }

    private void fillGapsWithLava(double screenHeight) {
        // Gap zwischen Plattform 1 (0-450) und 2 (500-700)
        this.blocks.add(createLavaColumn(450, screenHeight - 300, 50, 300));

        // Gap zwischen Plattform 2 (500-700) und 3 (780-930)
        this.blocks.add(createLavaColumn(700, screenHeight - 350, 80, 350));
    }

    private static LavaBlock createLavaColumn(double x, double y, double width, double height) {
        LavaBlock lava = new LavaBlock(new Location(x, y));
        lava.setWidth(width);
        lava.setHeight(height);
        return lava;
    }
}
