package de.cyzetlc.hsbi.game;

import de.cyzetlc.hsbi.game.audio.Music;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.event.EventManager;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.screens.LoadingScreen;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.gui.screens.SettingsScreen;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.level.impl.BossLevel;
import de.cyzetlc.hsbi.game.level.impl.SecondLevel;
import de.cyzetlc.hsbi.game.level.impl.TutorialLevel;
import de.cyzetlc.hsbi.game.listener.KeyListener;
import de.cyzetlc.hsbi.game.listener.PacketListener;
import de.cyzetlc.hsbi.game.listener.PlayerListener;
import de.cyzetlc.hsbi.game.listener.UserMessageListener;
import de.cyzetlc.hsbi.game.utils.json.JsonConfig;
import de.cyzetlc.hsbi.message.MessageHandler;
import de.cyzetlc.hsbi.network.Client;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code Game} class serves as the main application entry point and the central
 * singleton container for all core components of the game, including the JavaFX application
 * lifecycle, configuration, screen management, event system, and global physics constants.
 * <p>
 * It extends {@code Application} to manage the primary {@code Stage} and executes all
 * initial setup routines upon startup, such as loading configuration, initializing the
 * network client, and setting up screens and event listeners.
 *
 * @see ScreenManager
 * @see SoundManager
 * @see EventManager
 * @see Client
 *
 * @author Tom Coombs
 */
public class Game extends Application {
    /**
     * The SLF4J logger instance used for all application logging.
     * Initialized in the {@code start} method.
     */
    @Getter
    private static Logger logger;

    /**
     * The static reference to the main player entity in the game world.
     */
    public static EntityPlayer thePlayer;

    /**
     * Configuration object loaded from {@code config.json}, storing persistent settings
     */
    @Getter
    private JsonConfig config;

    /**
     * The static singleton instance of the {@code Game} class, providing global access
     * to all managed components.
     */
    @Getter
    private static Game instance;

    /**
     * The manager responsible for handling the main window, the game loop, and screen transitions.
     */
    @Getter
    private ScreenManager screenManager;

    /**
     * The currently active game level being played.
     */
    @Getter @Setter
    private Level currentLevel;

    /**
     * The initialized instance of the main menu screen.
     */
    @Getter @Setter
    private MainMenuScreen mainMenuScreen;

    /**
     * The initialized instance of the settings configuration screen.
     */
    @Getter @Setter
    private SettingsScreen settingsScreen;

    /**
     * A temporary reference to the screen the user should return to after an action (e.g., exiting settings).
     */
    @Getter @Setter
    private GuiScreen backScreen;

    /**
     * The network client responsible for connecting to and communicating with the game server.
     */
    @Getter
    private Client client;

    /**
     * Handler responsible for loading, storing, and retrieving game messages and translations from the configuration.
     */
    @Getter
    private MessageHandler messageHandler;

    /**
     * Global constant defining the strength of gravity applied to entities (pixels per second squared).
     */
    public static double gravity = 15;

    /**
     * Global constant defining the default horizontal movement speed for entities (pixels per second).
     */
    public static double moveSpeed = 450;

    /**
     * Global constant defining the default vertical upward velocity applied during a jump (pixels per second).
     */
    public static double jumpPower = 800;

    /**
     * The entry point for the JavaFX application lifecycle. This method is called after {@code main()}
     * and performs the complete initialization of the game system.
     * <p>
     * Initialization steps include:
     * <ul>
     * <li>Setting up logging and the singleton instance.</li>
     * <li>Initializing the network client and connecting.</li>
     * <li>Loading {@code config.json} and applying sound settings.</li>
     * <li>Registering all game event listeners (Packet, Key, Player, UserMessage).</li>
     * <li>Initializing and showing the {@code LoadingScreen}.</li>
     * <li>Setting the last known level from the config (Tutorial, Second, or Boss).</li>
     * <li>Starting the background menu music.</li>
     * </ul>
     *
     * @param primaryStage The primary stage provided by the JavaFX runtime.
     */
    @Override
    public void start(Stage primaryStage) {
        System.setProperty("startTime",
                new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date()));

        logger = LoggerFactory.getLogger(Game.class.getName());
        instance = this;
        thePlayer = new EntityPlayer();

        client = new Client();
        client.connectAndRun();

        getLogger().info("Starting Steal The Files v0.1 BETA..");
        getLogger().info("Loading configuration..");

        config = new JsonConfig("./config.json");
        messageHandler = new MessageHandler();
        messageHandler.loadFromJson(config.getObject().getJSONObject("messages"));

        getLogger().info("Configuration loaded successfully!");
        getLogger().info("Applying sound-settings from config..");

        SoundManager.setMuted(this.config.getObject().getBoolean("soundMuted"));
        SoundManager.setVolume(this.config.getObject().getDouble("soundVolume"));

        getLogger().info("SoundManger loaded successfully!");
        getLogger().info("Registering EventListener..");

        EventManager.register(new PacketListener());
        EventManager.register(new UserMessageListener());
        EventManager.register(new KeyListener());
        EventManager.register(new PlayerListener());

        getLogger().info("EventListener registered successfully!");

        getLogger().info("Loading ScreenManger..");

        screenManager = new ScreenManager(primaryStage);
        mainMenuScreen = new MainMenuScreen(screenManager);
        settingsScreen = new SettingsScreen(screenManager);
        screenManager.showScreen(new LoadingScreen(screenManager));

        getLogger().info("ScreenManager loaded & displayed MainMenu successfully!");
        getLogger().info("Client started successfully!");

        switch (this.config.getObject().getString("currentLevel")) {
            case "Tutorial" -> this.setCurrentLevel(new TutorialLevel());
            case "Second" -> this.setCurrentLevel(new SecondLevel());
            case "Boss" -> this.setCurrentLevel(new BossLevel());
        }

        SoundManager.playBackground(Music.MENU, true);
    }

    /**
     * The method called by the JavaFX runtime when the application is requested to shut down.
     */
    @Override
    public void stop()  {
        getLogger().info("Shutting down..");
        client.closeConnection();
    }
}

