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
 * The {@code GameScreen} class represents the central in-game state and is responsible
 * for both the visual representation and the core logic of the running game.
 * <p>
 * This class acts as the main controller for the gameplay session and handles the following responsibilities:
 * <ul>
 * <li><b>Rendering:</b> Draws the player, level elements (blocks, platforms), animated backgrounds, and the HUD (Heads-Up Display).</li>
 * <li><b>Physics Engine:</b> Calculates gravity, movement velocity, and collision detection against platforms, barriers, and enemies.</li>
 * <li><b>Camera Control:</b> Manages the viewport coordinates (CameraX/Y) with a smooth scrolling algorithm to track the player.</li>
 * <li><b>Input Handling:</b> Processes user input for movement (WASD), jumping (Space), interaction (E), and UI controls.</li>
 * <li><b>Game State Management:</b> Handles win/loss conditions (Game Over), pausing logic, and scene transitions.</li>
 * </ul>
 * <p>
 * Additionally, this class implements extensive debugging tools accessible via function keys:
 * <ul>
 * <li><b>F1:</b> Toggles tooltips, quest progress, and help text.</li>
 * <li><b>F2:</b> Toggles the technical debug bar (FPS, coordinates, etc.).</li>
 * <li><b>F3:</b> Toggles "NoClip" and "GodMode" (flight and invulnerability).</li>
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
     * The root container for all visual elements displayed on the screen.
     */
    protected final Pane root = new Pane();

    /**
     * Reference to the ScreenManager, used for handling screen transitions (e.g., Pause, Game Over).
     */
    protected final ScreenManager screenManager;

    /**
     * The main player entity.
     */
    private EntityPlayer player;

    /**
     * Current velocity component in the X-direction (horizontal movement).
     */
    private double dx = 1;

    /**
     * Current velocity component in the Y-direction (vertical movement/gravity).
     */
    private double dy = 0.5;

    /**
     * Text label used to display debug information (e.g., FPS, coordinates).
     */
    private Text debugLbl;

    /**
     * Flag indicating whether the game is currently paused.
     */
    private boolean paused = false;

    /**
     * The translucent overlay Pane displayed when the game is paused.
     */
    private Pane pauseOverlay;

    /**
     * Flag ensuring the Game Over sequence is triggered only once to prevent
     * multiple screen transitions.
     */
    private boolean gameOverTriggered = false;

    /**
     * The current X-coordinate of the camera/viewport in the game world.
     * This value determines the horizontal offset for rendering game elements.
     */
    @Getter
    private double cameraX = 0;

    /**
     * The current Y-coordinate of the camera/viewport in the game world.
     * This value determines the vertical offset for rendering game elements.
     */
    @Getter
    private double cameraY = 0;

    /**
     * Smoothing factor (interpolation value) used to gradually move the camera
     * towards the target position, creating a smooth follow effect. (0.0 to 1.0)
     */
    private final double cameraSmooth = 0.1; // wie schnell die Kamera folgt

    /**
     * Horizontal margin (dead zone) distance in pixels. The camera only starts
     * following the player when they move outside this margin.
     */
    private final double marginX = 400;

    /**
     * Vertical margin (dead zone) distance in pixels. The camera only starts
     * following the player when they move outside this margin.
     */
    private final double marginY = 150;

    /**
     * Text label used to display hints related to the Flipper item and interaction (KeyCode.E).
     */
    private Text flipperHint;

    /**
     * Flag to track if the Flipper interaction hint has already been displayed to the player.
     */
    private boolean flipperHintShown = false;

    /**
     * Text label displaying the current main quest objective.
     */
    private Text questLbl;

    /**
     * Text label showing the player's progress in collecting files (e.g., "Files: 3/5").
     */
    private Text filesProgressLbl;

    /**
     * The total number of collectible FolderBlock items present in the current level.
     */
    private int totalFolderCount = 0;

    /**
     * Toggle state for displaying user-facing tooltips and quest information (controlled by F1).
     */
    @Getter @Setter
    private boolean showTooltips = true; // F1

    /**
     * Text label displaying general control hints (e.g., F1/F2/F3 instructions).
     */
    private Text tipsLbl;

    /**
     * A list of ImageView objects representing the heart icons (full, half, empty)
     * used to visually display the player's health in the HUD.
     */
    private List<ImageView> heartImageViews;

    /**
     * Constructs a new GameScreen.
     *
     * @param screenManager The screen manager instance responsible for handling screen transitions.
     */
    public GameScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.heartImageViews = new ArrayList<>();
    }

    /**
     * Initializes the GameScreen and sets up the entire scene.
     * <p>
     * This method is called once when switching to this screen and performs all necessary
     * initial setup of the graphical interface, game state, and controls.
     * The steps include:
     * <ul>
     * <li>Clearing all previous elements from the root pane.</li>
     * <li>Drawing the animated background.</li>
     * <li>Setting the player at the start position and ensuring health points are correctly initialized.</li>
     * <li>Clearing and reloading all {@code Blocks} and {@code Platforms} of the current level.</li>
     * <li>Drawing static HUD elements, such as the "Back" and "Pause" buttons, debug text fields, and the health bar display.</li>
     * <li>Setting up the pause overlay and configuring the initial HUD layout positions.</li>
     * </ul>
     * This method ensures the screen is ready for the logical processing within the {@code update} cycle.
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
         * Remove all blocks & platforms from current level and
         * load draw current level
         */
        Game.getInstance().getCurrentLevel().getBlocks().clear();
        Game.getInstance().getCurrentLevel().getPlatforms().clear();
        Game.getInstance().getCurrentLevel().draw(width, height, root);

        // Draws the two buttons displayed on the top left
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
     * Executes the main game loop logic, running once per frame to update the game state.
     * <p>
     * This method is responsible for all dynamic game elements, including physics, input handling,
     * collision resolution, camera movement, and HUD updates.
     * <p>
     * Key operations performed in this method include:
     * <ul>
     * <li>Processing frame-rate independent physics (gravity, velocity calculation).</li>
     * <li>Handling standard player input for movement, jumping, and interaction (E).</li>
     * <li>Managing debug input toggles (F1) for tooltips.</li>
     * <li>Comprehensive collision detection against {@code Platforms} and various types of {@code Blocks}.</li>
     * <li>Processing specific interactions, such as collecting items (Flipper), activating barriers (GasBarrier), and enemy combat (RobotEnemyBlock).</li>
     * <li>Updating the camera position based on player location for smooth scrolling.</li>
     * <li>Checking for Game Over conditions (loss of health or falling out of the world).</li>
     * <li>Updating all dynamic HUD elements (health percentage, folder progress, debug status).</li>
     * </ul>
     *
     * @param delta The time elapsed since the last frame, used to ensure physics calculations
     * are independent of the frame rate.
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

        double gravity = Game.gravity;       // gravity strength
        double moveSpeed = Game.moveSpeed;    // horizontal speed
        double jumpPower = Game.jumpPower;    // jump power
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

        // input
        if (screenManager.getInputManager().isPressed(KeyCode.A)) {
            dx = -moveSpeed * delta;
            player.setDirection(Direction.WALK_LEFT);
        } else if (screenManager.getInputManager().isPressed(KeyCode.D)) {
            dx = moveSpeed * delta;
            player.setDirection(Direction.WALK_RIGHT);
        } else {
            dx = 0;
        }

        // gravity
        dy += gravity * delta;

        // tentative position
        double nextX = x + dx;
        double nextY = y + dy;

        Rectangle2D nextBounds = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());

        // platform collisions
        for (Platform platform : Game.getInstance().getCurrentLevel().getPlatforms()) {
            Rectangle2D pBounds = platform.getBounds();
            platform.update(this);

            if (nextBounds.intersects(pBounds)) {
                // landing from above
                if (y + player.getHeight() <= platform.getY()) {
                    nextY = platform.getY() - player.getHeight();
                    dy = 0;
                    onGround = true;
                }
                // collision from below
                if (dy < 0 && y >= platform.getY() + platform.getHeight() && nextY <= platform.getY() + platform.getHeight()) {
                    nextY = platform.getY() + platform.getHeight();
                    hittingCeiling = true;
                    dy = 0;
                } else {
                    hittingCeiling = false;
                }

                // side collision
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

        // block collisions
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
                LaserBlock laser = enemy.tryFire(player);
                if (laser != null) {
                    laser.draw(root);
                    pendingBlocks.add(laser);
                }
            }

            if (nextBounds.intersects(pBounds) && block.isActive() && !player.isNoClipEnabled()) {
                if (block instanceof GasBarrierBlock barrier && interactPressed && player.hasFlipper()) {
                    barrier.deactivate();
                    continue;
                }
                if (block instanceof RobotEnemyBlock enemy) {
                    double enemyTop = enemy.getLocation().getY();
                    boolean stomp = (y + player.getHeight() <= enemyTop + 6) && dy > 0;
                    if (stomp) {
                        enemy.kill();
                        nextY = enemyTop - player.getHeight();
                        dy = -jumpPower * delta * 0.6;
                    } else {
                        enemy.hitPlayer(player);
                    }
                    // continue with collision resolution but skip duplicate onCollide
                } else {
                    block.onCollide(player);
                }

                if (block.isCollideAble()) {
                    // landing from above
                    if (y + player.getHeight() <= block.getLocation().getY()) {
                        nextY = block.getLocation().getY() - player.getHeight();
                        nextX += block.getDeltaX(); // follow moving platform x movement
                        dy = 0;
                        onGround = true;
                    }
                    // left
                    else if (x + player.getWidth() <= block.getLocation().getX()) {
                        nextX = block.getLocation().getX() - player.getWidth();
                        dx = 0;
                    }
                    // right
                    else if (x >= block.getLocation().getX() + block.getWidth()) {
                        nextX = block.getLocation().getX() + block.getWidth();
                        dx = 0;
                    }
                }
            }
        }
        if (!pendingBlocks.isEmpty()) {
            blocks.addAll(pendingBlocks);
        }

        // window bounds
        if (nextX < 0) nextX = 0;

        // fell out of the world -> game over
        double screenNextY = nextY - this.cameraY;
        if (screenNextY + player.getHeight() > height) {
            if (player.getHealth() > 0) {
                player.setHealth(0);
            }
            this.handleGameOver();
            return;
        }

        // jump (only if on ground)
        if (screenManager.getInputManager().isPressed(KeyCode.SPACE) && dy == 0 && !hittingCeiling) {
            dy = -jumpPower * delta;
            player.setDirection(Direction.JUMP);
        }

        // apply position
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
     * Updates the position of the camera viewport to follow the player with a smooth scrolling effect.
     * <p>
     * This method implements a "dead zone" logic: the camera only begins to move when the player's
     * screen position exceeds the predefined margins ({@code marginX} and {@code marginY}).
     * The movement is then smoothed using the {@code cameraSmooth} factor to prevent jerky transitions.
     * <p>
     * The camera is also clamped to prevent viewing coordinates less than zero, ensuring the viewport
     * stays within the intended level boundaries, unless NoClip mode is active.
     *
     * @param width  The width of the screen/stage, used for calculating the right margin boundary.
     * @param height The height of the screen/stage, used for calculating the bottom margin boundary.
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
     * Creates and initializes the visual overlay displayed when the game is paused.
     * It includes a transparent black background, a "Pause" title, and control buttons
     * (Continue and Back to Menu).
     *
     * @param width The current width of the game window/stage.
     * @param height The current height of the game window/stage.
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
     * Toggles the paused state of the game.
     * If the game is paused, it resumes, and the pause overlay is hidden.
     * If the game is running, it pauses, and the pause overlay is shown.
     */
    private void togglePause() {
        this.paused = !this.paused;
        if (this.pauseOverlay != null) {
            this.pauseOverlay.setVisible(this.paused);
        }
    }

    /**
     * Handles the Game Over state.
     * This is triggered when the player's health drops to zero, or they fall out of the world.
     * It prevents multiple triggers and transitions the screen back to the Main Menu.
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
     * Draws the player's health bar visually using heart icons (full, half, empty).
     * The health display is aligned to the top-right corner of the screen.
     *
     * @param width The current width of the game window/stage, used for right-side alignment.
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
     * Sets the consistent vertical positions for all heads-up display (HUD) text elements.
     * This method ensures text blocks like debug information, tips, and quest progress
     * are neatly spaced below the primary control buttons (Back, Pause).
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
     * Updates and formats the debug information displayed in the top-left corner (Debug Bar).
     * This bar shows technical details like FPS, player state, camera coordinates, and game parameters.
     * It is only visible if the {@code showDebugBar} flag is true (toggled by F2).
     *
     * @param onGround Indicates if the player is currently standing on a surface.
     * @param moveSpeed The current horizontal movement speed parameter.
     * @param jumpPower The current vertical jump power parameter.
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
     * Counts the total number of folder blocks (collectible files) present in the current level.
     *
     * @return The total count of {@code FolderBlock} instances in the level's block list.
     */
    private int countFolderBlocks() {
        return (int) Game.getInstance().getCurrentLevel().getBlocks().stream()
                .filter(block -> block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FolderBlock)
                .count();
    }

    /**
     * Counts the number of active (uncollected) folder blocks remaining in the current level.
     *
     * @return The count of {@code FolderBlock} instances that are currently active.
     */
    private int countActiveFolders() {
        return (int) Game.getInstance().getCurrentLevel().getBlocks().stream()
                .filter(block -> block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FolderBlock)
                .filter(de.cyzetlc.hsbi.game.gui.block.Block::isActive)
                .count();
    }

    /**
     * Updates the HUD element showing the file collection progress.
     * It calculates the number of collected files against the total available files
     * and sets the text for {@code filesProgressLbl}.
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
     * Retrieves the root pane of the GameScreen, which contains all visual elements.
     *
     * @return The JavaFX {@code Pane} used as the root container.
     */
    @Override
    public Pane getRoot() {
        return root;
    }

    /**
     * Returns the identifying name of this screen.
     *
     * @return The constant screen name "GameScreen".
     */
    @Override
    public String getName() {
        return "GameScreen";
    }
}
