# Stichpunktartig-für-Tom (V2 – detailliert)

## Projekt-Basis
- **Projektname:** WS2526-P1 („Steal The Files“)
- **Kurzbeschreibung:** 2D-JavaFX-Plattformer mit Client/Server-Kommunikation; Bewegung, Items/Gegner/Gasbarrieren, Score-Transfer; HUD mit Quest-/Fortschrittsanzeige.
- **Zielgruppe:** Studierende/Lehrende im Kurs „Programmieren 1“ (Lehr-/Übungsprojekt).
- **Verantwortliche:** @CyZeTLC, @Phantomic813 (weitere nicht dokumentiert).

## Technischer Stack
- **Sprachen:** Java 19 (Corretto 19.0.2), JavaFX 17.0.17.
- **Frameworks/Libs:** SLF4J + Log4j (Logging), Lombok, Gson (Core), OSHI (Client HW-Infos), eigene Event-/Packet-Engine.
- **Datenbank:** MySQL/MariaDB (Connector 8.0.30, MariaDB 3.0.6) + HikariCP vorgesehen (Stats/Highscores).
- **Hosting/Infra:** Lokaler TCP-Socket 25570; Maven Multi-Module (core, client, server); optionale jlink-Bundler (`build-scripts`); Logs unter `logs/`.

## Ordner- & Dateistruktur
- **`pom.xml`** Multi-Module (`core`, `client`, `server`).
- **`/core`** Event-System, Entities, Netzwerk-Pakete, JSON-/DB-Utilities.
- **`/client`** JavaFX-Spiel (Screens, Input, Audio), Netzwerk-Client; Assets unter `client/src/main/resources/assets`.
- **`/server`** TCP-Server, Packet-/Message-Listener, vorbereiteter DB-Handler.
- **`/config.json`** Spiel- und DB-Konfiguration (Level, Sound, Sprache, DB-Zugang – aktuell Klartext).
- **`/assets`** Globale Styles/Bilder/Audio (Spiegelungen im Client-Assets-Ordner).
- **`/docs`** HTML/MD/IPYNB-Dokumentation; **`/logs`** Laufzeit-Logs; **`/build-scripts`** bundling Batchfiles.
- **`src/main/java/de/cyzetlc/hsbi/Main.java`** IntelliJ-Template (nicht verwendet).

## Wichtige Module & Logik
- **Event-System (`core/.../event`):** `EventManager.register(...)` meldet Listener an; `Event.call()` dispatcht; `EventCancelable` erlaubt Abbruch; Priorisierung über `EventPriority`.
- **Netzwerk-Pakete (`core/.../packets`):** Serializable Packets (z.B. `ClientLoginPacket`, `ClientDataPacket` für Heartbeats, `ClientSubmitScorePacket`, `UserMessagePacket`, `ServerSendHighscoresPacket`); `SerializationUtils` nutzt Java-Object-Streams (keine Whitelist/Versionierung).
- **Client-Spiel (`client/.../game/Game.java`, `.../gui`, `.../level`):** Lädt Config, Sound, Screens; `ScreenManager` steuert Stage, Game-Loop-Thread, FPS; `GameScreen.update()` Physik (Gravity/Jump), Kollisionen (Plattformen/Blöcke/Enemy-Laser), Kamera-Follow, HUD (Quest, Files, Health), Interaktionen (Flipper/Gasbarriere, RobotEnemy).
- **Netzwerk-Client (`client/.../network/Client.java`):** Verbindet zu `localhost:25570`, sendet Login, startet `ReceiverTask` (Packets→Events) und `SenderTask` (20 Ticks/s `ClientDataPacket`), `sendFinalScore()` für Highscores.
- **Server (`server/.../Server.java`, Listener):** ServerSocket 25570; pro Client ein `MultiClientHandler`-Thread (periodisch `UserMessagePacket`, liest/dispatcht Packets); `PacketListener` behandelt Score/Community/UserMessage/Login/Data; `UserMessageListener` verarbeitet Textkommandos (Exit/clients/Echo).
- **DB/Stats (`server/.../utils/GlobalStatsHandler.java`):** `saveBestScore(...)` INSERT in `stats` (async); benötigt aktiven `QueryHandler` aus Config.
- **Levels & Blöcke:** Level-Implementierungen `TutorialLevel`, `SecondLevel`, `BossLevel`, `CommunityLevel`; Block-Typen u.a. `RobotEnemyBlock` (schießt `LaserBlock`), `GasBarrierBlock`, `JumpBoostBlock`, `SpeedBoostBlock`, `USBStickBlock`, `FinishBlock`, `FolderBlock`, `FloatingPlatformBlock`, `LavaBlock`.

## Datenfluss & Speicher
- **Eingabe:** Tastatur-Events via `InputManager`; eingehende Netzwerk-Pakete vom Server.
- **Verarbeitung:** GameLoop (Physik, Kollision, Kamera, HUD), PacketListener → Event-System, optionale DB-Speicherung von Scores (derzeit nicht aktiv, da MySQL-Init fehlt).
- **Ausgabe:** JavaFX-UI (Screens, HUD, Animation), Netzwerk-Pakete an Server/Clients, Logs unter `logs/*.log`.

## Setup für Entwickler
- Repo klonen.
- `.env` nicht genutzt; `config.json` anpassen (Sound, Sprache, Level, vor allem DB-Zugang ersetzen!).
- Installieren: `mvn clean install`
- Client starten: `cd client && mvn javafx:run`
- Server starten: `cd server && mvn exec:java -Dexec.mainClass=de.cyzetlc.hsbi.Server`
- DB: Tabelle `stats` anlegen; MySQL-Zugang in `config.json` setzen; `buildMySQLConnection()` aktivieren/aufrufen, damit `QueryHandler` initialisiert wird.

## Offene Punkte / Known Issues
- [ ] Klartext-DB-Credentials in `config.json` (Sicherheitsrisiko).
- [ ] `buildMySQLConnection()` wird nie aufgerufen → DB/Stats-Funktionalität inaktiv.
- [ ] Java-Serialization ohne Whitelist/Versionierung (RCE-/Kompatibilitätsrisiko).
- [ ] Kein Auth/Handshake vor Paketannahme; Clients vertrauen Pakete blind.
- [ ] Physik-/Kollisionslogik in `GameScreen` komplex und ungetestet; starker Coupling zu GUI.
- [ ] Große Assets/Logs im Repo (Bereinigung/`.gitignore` prüfen).
