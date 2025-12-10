# Stichpunktartig-für-Tom (V1)

## Projekt-Basis
- **Projektname:** WS2526-P1 („Steal The Files“)
- **Kurzbeschreibung:** JavaFX-Plattformer mit Client/Server-Architektur; Spieler läuft, springt und interagiert, während Positions- und Score-Daten über TCP ausgetauscht werden.
- **Zielgruppe:** Studierende/Lehrende im Kurs „Programmieren 1“.
- **Verantwortliche:** @CyZeTLC, @Phantomic813 (weitere nicht hinterlegt).

## Technischer Stack
- **Sprachen:** Java 19 (Corretto 19.0.2), JavaFX 17.
- **Frameworks/Libs:** SLF4J + Log4j, Lombok, Gson (Core), OSHI (Client), eigene Event-/Packet-Engine.
- **Datenbank:** MySQL/MariaDB (Connector 8.0.30, MariaDB 3.0.6) mit HikariCP vorgesehen.
- **Hosting/Infra:** Lokaler TCP-Socket 25570, Maven Multi-Module, optionale jlink-Bundler (`build-scripts`).

## Ordner- & Dateistruktur
- **`pom.xml`** Multi-Module (`core`, `client`, `server`).
- **`/core`** Events, Entities, Netzwerk-Pakete, JSON-/DB-Utils.
- **`/client`** JavaFX-Spiel, Screens, Input, Netzwerk-Client; Assets unter `client/src/main/resources/assets`.
- **`/server`** TCP-Server, Listener, DB-Handler.
- **`/config.json`** Spiel- und DB-Konfig (Credentials im Klartext).
- **`/assets`** Globale Styles/Bilder/Audio (teils dupliziert im Client).
- **`/docs`** HTML/MD/IPYNB-Doku; **`/logs`** Laufzeit-Logs; **`/build-scripts`** bundling Batchfiles.
- **`src/main/java/de/cyzetlc/hsbi/Main.java`**: IntelliJ-Template, nicht genutzt.

## Wichtige Module & Logik
- **Event-System (`core/.../event`)**: `EventManager.register(...)` meldet Listener an, `Event.call()` dispatcht; `EventCancelable` erlaubt Abbruch.
- **Netzwerk-Pakete (`core/.../packets`)**: Serializable Packets (Login, ClientData, SubmitScore, Messages); `SerializationUtils` nutzt Java-Object-Streams.
- **Client (`client/.../game/Game.java`, `.../network/Client.java`)**: Startet JavaFX, lädt Config, registriert Listener; `Client.connectAndRun()` startet Receiver/Sender; `GameScreen.update()` Physik/Kollision/Kamera/HUD.
- **Server (`server/.../Server.java`, Listener)**: ServerSocket 25570, Thread pro Client (`MultiClientHandler`), Packet/Event-Routing; MySQL-Anbindung vorbereitet, aber nicht aktiviert.

## Datenfluss & Speicher
- **Eingabe:** Tastatur (InputManager), Netzwerk-Pakete vom Server.
- **Verarbeitung:** GameLoop (Physik, Kollision, Kamera, HUD), PacketListener → Events, optionale DB-Speicherung (derzeit inaktiv).
- **Ausgabe:** JavaFX-UI, Netzwerk-Pakete (Scores/Updates), Logs in `logs/*.log`.

## Setup für Entwickler
- Repo klonen.
- `.env` nicht genutzt; `config.json` bearbeiten (Credentials ersetzen!).
- Installieren: `mvn clean install`
- Client starten: `cd client && mvn javafx:run`
- Server starten: `cd server && mvn exec:java -Dexec.mainClass=de.cyzetlc.hsbi.Server`
- DB optional: Tabelle `stats` anlegen, MySQL-Zugang setzen, `buildMySQLConnection()` aktivieren.

## Offene Punkte / Known Issues
- [ ] Klartext-DB-Credentials in `config.json`.
- [ ] `buildMySQLConnection()` nie aufgerufen → DB/Stats inaktiv.
- [ ] Java-Serialization ohne Whitelist/Versionierung (Sicherheits-/Kompatibilitätsrisiko).
- [ ] Kein Auth/Handshake vor Paketannahme.
- [ ] Physik/Kollision (`GameScreen`) komplex, ungetestet.
- [ ] Große Assets/Logs im Repo (Bereinigung/Ignore prüfen).
