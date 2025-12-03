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
import de.cyzetlc.hsbi.network.Client;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game extends Application {
    @Getter
    private static Logger logger;

    public static EntityPlayer thePlayer;

    @Getter
    private JsonConfig config;

    @Getter
    private static Game instance;

    @Getter
    private ScreenManager screenManager;

    @Getter @Setter
    private Level currentLevel;

    @Getter
    private MainMenuScreen mainMenuScreen;

    @Getter
    private SettingsScreen settingsScreen;

    @Getter @Setter
    private GuiScreen backScreen;

    @Getter
    private Client client;

    public static double gravity = 15;       // Stärke der Schwerkraft
    public static double moveSpeed = 450;    // horizontale Bewegungsgeschwindigkeit (Pixel/Sek)
    public static double jumpPower = 800;    // Sprungkraft

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

    @Override
    public void stop()  {
        getLogger().info("Shutting down..");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

