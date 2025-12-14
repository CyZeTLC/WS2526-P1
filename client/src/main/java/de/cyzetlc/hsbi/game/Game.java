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
 * Die Klasse {@code Game} dient als Haupt-Einstiegspunkt der Anwendung und als zentrale
 * Singleton-Container für alle Kernkomponenten des Spiels. Dazu gehören der JavaFX-Anwendungslebenszyklus,
 * die Konfiguration, das Screen-Management, das Event-System und globale Physikkonstanten.
 *
 * <p>
 * Sie erweitert {@code Application}, um die primäre {@code Stage} zu verwalten, und führt bei
 * Programmstart alle anfänglichen Setup-Routinen aus, wie das Laden der Konfiguration, die Initialisierung
 * des Netzwerk-Clients und das Einrichten von Screens und Event-Listenern.
 *
 * @see ScreenManager
 * @see SoundManager
 * @see EventManager
 * @see Client
 *
 * @author Tom Coombs
 * @author leonardo (aka. Phantomic)
 */
public class Game extends Application {
    /**
     * Die SLF4J Logger-Instanz, die für alle Anwendungs-Logmeldungen verwendet wird.
     * Initialisiert in der {@code start}-Methode.
     */
    @Getter
    private static Logger logger;

    /**
     * Die statische Referenz auf die Hauptspieler-Entität in der Spielwelt.
     */
    public static EntityPlayer thePlayer;

    /**
     * Das Konfigurationsobjekt, geladen aus {@code config.json}, speichert persistente Einstellungen.
     */
    @Getter
    private JsonConfig config;

    /**
     * Die statische Singleton-Instanz der {@code Game}-Klasse, die globalen Zugriff
     * auf alle verwalteten Komponenten bietet.
     */
    @Getter
    private static Game instance;

    /**
     * Der Manager, der für die Verwaltung des Hauptfensters, des Spiel-Loops und der Bildschirmübergänge zuständig ist.
     */
    @Getter
    private ScreenManager screenManager;

    /**
     * Das aktuell aktive Level, das gerade gespielt wird.
     */
    @Getter @Setter
    private Level currentLevel;

    /**
     * Die initialisierte Instanz des Hauptmenü-Bildschirms.
     */
    @Getter @Setter
    private MainMenuScreen mainMenuScreen;

    /**
     * Die initialisierte Instanz des Einstellungs-Konfigurationsbildschirms.
     */
    @Getter @Setter
    private SettingsScreen settingsScreen;

    /**
     * Eine temporäre Referenz auf den Bildschirm, zu dem der Benutzer nach einer Aktion
     * zurückkehren soll (z. B. nach dem Verlassen der Einstellungen).
     */
    @Getter @Setter
    private GuiScreen backScreen;

    /**
     * Der Netzwerk-Client, der für die Verbindung mit und die Kommunikation mit dem Spielserver zuständig ist.
     */
    @Getter
    private Client client;

    /**
     * Der Handler, der für das Laden, Speichern und Abrufen von Spielnachrichten und Übersetzungen aus der Konfiguration zuständig ist.
     */
    @Getter
    private MessageHandler messageHandler;

    /**
     * Globale Konstante, die die Stärke der auf Entitäten angewendeten Schwerkraft definiert (Pixel pro Sekunde im Quadrat).
     */
    public static double gravity = 15;

    /**
     * Globale Konstante, die die Standard-Horizontalbewegungsgeschwindigkeit für Entitäten definiert (Pixel pro Sekunde).
     */
    public static double moveSpeed = 450;

    /**
     * Globale Konstante, die die Standard-Vertikalgeschwindigkeit definiert, die beim Sprung angewendet wird (Pixel pro Sekunde).
     */
    public static double jumpPower = 800;

    /**
     * Der Einstiegspunkt für den JavaFX-Anwendungslebenszyklus. Diese Methode wird nach {@code main()}
     * aufgerufen und führt die vollständige Initialisierung des Spielsystems durch.
     * <p>
     * Die Initialisierungsschritte umfassen:
     * <ul>
     * <li>Einrichtung von Logging und der Singleton-Instanz.</li>
     * <li>Initialisierung des Netzwerk-Clients und Verbindung.</li>
     * <li>Laden von {@code config.json} und Anwenden der Sound-Einstellungen.</li>
     * <li>Registrierung aller Spiel-Event-Listener (Packet, Key, Player, UserMessage).</li>
     * <li>Initialisierung und Anzeige des {@code LoadingScreen}.</li>
     * <li>Setzen des zuletzt bekannten Levels aus der Konfiguration (Tutorial, Second oder Boss).</li>
     * <li>Starten der Hintergrundmenü-Musik.</li>
     * </ul>
     *
     * @param primaryStage Die primäre Stage, die von der JavaFX-Laufzeitumgebung bereitgestellt wird.
     */
    @Override
    public void start(Stage primaryStage) {
        System.setProperty("startTime",
                new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date()));

        logger = LoggerFactory.getLogger(Game.class.getName());
        instance = this;
        thePlayer = new EntityPlayer();

        /* Mit diesem beiden Zeilen würde eine Verbindung zum Server hergestellt werden (Wenn der Server an ist, dann funktioniert diese Verbindung bereits)
         *
        client = new Client();
        client.connectAndRun();
         */

        getLogger().info("Starting Steal The Files v0.1 BETA..");
        getLogger().info("Loading configuration..");

        config = new JsonConfig("./config.json");

        if (!config.getObject().has("currentLevel")) {
            getLogger().error("Die Config.json ist nicht vollständig!");
            System.exit(-1);
            return;
        }

        messageHandler = new MessageHandler();
        messageHandler.loadFromJson(config.getObject().getJSONObject("messages"));

        getLogger().info("Configuration loaded successfully!");
        getLogger().info("Applying sound-settings from config..");

        SoundManager.setMuted(this.config.getObject().getBoolean("soundMuted"));
        SoundManager.setVolume(this.config.getObject().getDouble("soundVolume"));

        getLogger().info("SoundManger loaded successfully!");
        getLogger().info("Registering EventListener..");

        // Registrierung der zentralen Event-Listener
        EventManager.register(new PacketListener());
        EventManager.register(new UserMessageListener());
        EventManager.register(new KeyListener());
        EventManager.register(new PlayerListener());

        getLogger().info("EventListener registered successfully!");

        getLogger().info("Loading ScreenManger..");

        // Initialisiere die UI-Manager und zeige den Ladebildschirm
        screenManager = new ScreenManager(primaryStage);
        mainMenuScreen = new MainMenuScreen(screenManager);
        settingsScreen = new SettingsScreen(screenManager);
        screenManager.showScreen(new LoadingScreen(screenManager));

        getLogger().info("ScreenManager loaded & displayed MainMenu successfully!");
        getLogger().info("Client started successfully!");

        // Setze das letzte gespeicherte Level basierend auf der Konfigurationsdatei
        switch (this.config.getObject().getString("currentLevel")) {
            case "Tutorial" -> this.setCurrentLevel(new TutorialLevel());
            case "Second" -> this.setCurrentLevel(new SecondLevel());
            case "Boss" -> this.setCurrentLevel(new BossLevel());
        }

        // Starte die Hintergrundmusik
        SoundManager.playBackground(Music.MENU, true);
    }

    /**
     * Die Methode, die von der JavaFX-Laufzeitumgebung aufgerufen wird, wenn die Anwendung beendet werden soll.
     * Führt Aufräumarbeiten durch (z. B. Schließen der Netzwerkverbindung).
     */
    @Override
    public void stop()  {
        getLogger().info("Shutting down..");
        client.closeConnection();
    }

    /**
     * Die Hauptmethode der Anwendung.
     *
     * @param args Kommandozeilenargumente.
     */
    public static void main(String[] args) {
        launch(args);
    }
}