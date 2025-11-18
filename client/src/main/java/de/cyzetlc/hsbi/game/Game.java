package de.cyzetlc.hsbi.game;

import de.cyzetlc.hsbi.game.audio.Music;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.event.EventManager;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.level.Level;
import de.cyzetlc.hsbi.game.level.impl.TutorialLevel;
import de.cyzetlc.hsbi.game.listener.KeyListener;
import de.cyzetlc.hsbi.game.listener.PacketListener;
import de.cyzetlc.hsbi.game.listener.PlayerListener;
import de.cyzetlc.hsbi.game.listener.UserMessageListener;
import de.cyzetlc.hsbi.game.utils.database.mysql.MySQLCredentials;
import de.cyzetlc.hsbi.game.utils.database.mysql.QueryHandler;
import de.cyzetlc.hsbi.game.utils.json.JsonConfig;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game extends Application {
    @Getter
    private static final Logger logger = LoggerFactory.getLogger(Game.class.getName());

    public static EntityPlayer thePlayer;

    @Getter
    private JsonConfig config;

    @Getter
    private QueryHandler queryHandler;

    @Getter
    private static Game instance;

    @Getter
    private ScreenManager screenManager;

    @Getter @Setter
    private Level currentLevel;

    public static double gravity = 15;       // Stärke der Schwerkraft
    public static double moveSpeed = 450;    // horizontale Bewegungsgeschwindigkeit (Pixel/Sek)
    public static double jumpPower = 800;    // Sprungkraft

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        thePlayer = new EntityPlayer();

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

        this.buildMySQLConnection();

        getLogger().info("Loading ScreenManger..");

        screenManager = new ScreenManager(primaryStage);
        screenManager.showScreen(new MainMenuScreen(screenManager));

        getLogger().info("ScreenManager loaded & displayed MainMenu successfully!");
        getLogger().info("Client started successfully!");

        this.setCurrentLevel(new TutorialLevel());

        SoundManager.playBackground(Music.MENU, true);
    }

    /**
     * It creates a new QueryHandler object with the credentials from the config file, and then creates a table if it
     * doesn't exist
     */
    private void buildMySQLConnection() {
        getLogger().info("Building MySQL-Connection..");

        this.queryHandler = new QueryHandler(new JsonConfig(this.config.getObject().getJSONObject("mysql")).load(MySQLCredentials.class));
        //this.queryHandler.createBuilder("CREATE TABLE IF NOT EXISTS logs(numeric_id INT UNIQUE AUTO_INCREMENT, timestamp BIGINT, thread VARCHAR(64), guild_id BIGINT, text TEXT);").executeUpdateSync();
        //this.queryHandler.createBuilder("CREATE TABLE IF NOT EXISTS settings(numeric_id INT UNIQUE AUTO_INCREMENT, guild_id BIGINT, language VARCHAR(3), log_channel BIGINT, apply_channel BIGINT, verify_channel BIGINT, verify_webhook BIGINT, verify_webhook_url TEXT, verify_role BIGINT);").executeUpdateSync();

        getLogger().info("MySQL-Connection finished!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

