# Steal The Files - Dokumentation (DE)
JavaFX 2D-Plattformer (Client/Server/Core)

**Inhalt**
1. Überblick & Architektur
2. Setup & Start
3. Spiellogik (Code + Erklärung)
4. GUI/Navi (Code + Erklärung + Prompt)
5. Levelaufbau (Code + Erklärung)
6. Audio & Einstellungen (Code + Erklärung + Prompt)
7. Netzwerk & Daten (Code + Erklärung)
8. Debugging & offene Punkte (Code + Erklärung)
9. Roadmap

## 1. Überblick & Architektur
- Einstiegspunkt: `Game` startet JavaFX, lädt `config.json`, setzt Sound, verbindet Client (TCP) und legt Spieler (`EntityPlayer`) + Level an.
- `ScreenManager`: verwaltet Szenen, eigener Logik-Thread (~200 Hz) + Render über JavaFX-Thread, Fullscreen ohne Window-Chrome.
- Event-Layer (`EventManager`): verteilt Netzwerk-/Input-Events an Listener (Key/Packet/UserMessage/Player).
- Level-Auswahl nur hier gekoppelt an `config.currentLevel`.
```java
switch (config.getString("currentLevel")) {
    case "Tutorial" -> setCurrentLevel(new TutorialLevel());
    case "Second"   -> setCurrentLevel(new SecondLevel());
    case "Boss"     -> setCurrentLevel(new BossLevel());
}
```
- Globale Gameplay-Parameter: `gravity=15`, `moveSpeed=450 px/s`, `jumpPower=800` (werden dynamisch in der Physik benutzt).

## 2. Setup & Start
- Build: `mvn clean install` (Client/Server/Core), Java 17 + JavaFX benötigt.
- Start: Server und Client aus der IDE oder per `java -jar target/...` (Assets liegen unter `client/src/main/resources`).
- Config (`config.json`): `currentLevel`, `soundVolume`/`soundMuted`, `messages.game.prefix`, `mysql.*`, `tutorialFinished`.
- Client-Boot: lädt Sound-Settings, baut `ScreenManager`, zeigt `LoadingScreen`, wechselt ins `MainMenu` und startet Hintergrundmusik.
```java
SoundManager.setMuted(config.getBoolean("soundMuted"));
SoundManager.setVolume(config.getDouble("soundVolume"));
screenManager = new ScreenManager(primaryStage);
screenManager.showScreen(new LoadingScreen(screenManager));
```

## 3. Spiellogik (Code + Erklärung)
- Input-Debounce: `InputManager.pollJustPressed` sorgt für einmalige Toggles (F1/F2/F3) im Frame.
```java
boolean f1 = input.pollJustPressed(KeyCode.F1);
boolean f2 = input.pollJustPressed(KeyCode.F2);
boolean f3 = input.pollJustPressed(KeyCode.F3);
if (f1) showTooltips = !showTooltips;
if (f2) showDebugBar = !showDebugBar;
if (f3) { player.setNoClip(!player.isNoClipEnabled()); player.setGodMode(player.isNoClipEnabled()); }
```
- Bewegung/Physik (frame-unabhängig): Velocity aus `delta`, Gravitation, Jump, A/D für Lauf; NoClip umgeht Gravitation + Kollision.
```java
dy += Game.gravity * delta;
double nextX = x + dx;
double nextY = y + dy;
Rectangle2D next = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());
```
- Plattform-Kollision: trennt Landung, Decke, Seiten; übernimmt Delta von Moving Blocks.
```java
if (next.intersects(pBounds)) {
    if (y + player.getHeight() <= platform.getY()) { nextY = platform.getY() - player.getHeight(); dy = 0; onGround = true; }
    else if (x + player.getWidth() <= platform.getX()) { nextX = platform.getX() - player.getWidth(); dx = 0; }
}
```
- Blocks & Gegner: `GasBarrier` tötet ohne Flipper+E, `RobotEnemy` feuert `LaserBlock` mit 4s Lifetime, Stomp-Kill von oben, `FolderBlock` sammelt Files (erst nach USB).
```java
if (block instanceof GasBarrierBlock barrier && interactPressed && player.hasFlipper()) {
    barrier.deactivate();
    continue;
}
if (block instanceof RobotEnemyBlock enemy) {
    LaserBlock laser = enemy.tryFire(player);
    if (laser != null) pendingBlocks.add(laser);
}
```
- Kamera-Smoothing: Dead-Zone mit Lerp, getrennt für X/Y; in NoClip keine Clamps.
```java
double targetX = cameraX + (playerScreenX > width - marginX ? playerScreenX - (width - marginX) :
                           playerScreenX < marginX ? -(marginX - playerScreenX) : 0);
cameraX += (targetX - cameraX) * cameraSmooth;
```
- HUD: Herzen oben rechts, Quest/File-Progress, Tooltip-Bar; `GameScreen.togglePause()` blendet Overlay ein.

## 4. GUI/Navi (Code + Erklärung + Prompt)
- Screens: `LoadingScreen` (preload Assets), `MainMenu` (Scrolling-Background, Systeminfo/Progress-Anzeige), `Settings`, `Multiplayer`, `GameScreen`, `LevelFinished`.
- ScreenManager: Fullscreen, eigenes CSS (`assets/style.css`), Input-Registration und Game-Loop im Worker-Thread, Render per `Platform.runLater`.
- MainMenu-Animation: zwei Hintergründe werden endlos verschoben.
```java
Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
    bg1.setTranslateX(bg1.getTranslateX() + 1);
    bg2.setTranslateX(bg2.getTranslateX() + 1);
    if (bg1.getTranslateX() >= width) bg1.setTranslateX(bg2.getTranslateX() - width);
    if (bg2.getTranslateX() >= width) bg2.setTranslateX(bg1.getTranslateX() - width);
}));
timeline.setCycleCount(Animation.INDEFINITE);
timeline.play();
```
- Buttons: `UIUtils.drawCenteredButton` für MainMenu-CTAs; in GameScreen oben links `Zurück` + `Pause`.
- Prompt/Tooltips: F1 blendet Hilfetext grün ein, F2 Debug-Bar, F3 NoClip+God.

## 5. Levelaufbau (Code + Erklärung)
- Basisklasse `Level`: hält `platforms` + `blocks`, misst `levelStarted`, optional `nextLevel`.
- Lava-Autofill: `placeLavaBetweenPlatforms(sceneHeight)` sortiert Plattformen nach X und füllt horizontale Lücken mit `LavaBlock`-Säulen.
```java
ordered.sort(Comparator.comparingDouble(Platform::getX));
if (gapWidth > 1.0) blocks.add(createLavaColumn(gapStart, lavaY, gapWidth, lavaH));
```
- Tutorial: grundlegend, USB-Stick gated Files; Jump- und Speed-Buffs, FloatingPlatform, Finish rechts.
- Second: mehr vertikale Sprüngü, Flipper-Item vor Gas-Schranke, RobotEnemy-Boss am Ende, mehrere Folder-Collectibles.
- Boss: langer Boden, vier RobotEnemies, Finish kurz dahinter (reiner Kampf-Fokus).

## 6. Audio & Einstellungen (Code + Erklärung + Prompt)
- `SoundManager`: cached Media, spielt Effekte parallel, `playWithDuck` duckt Musik temporär, Hintergrundmusik via `playBackground` (loop optional).
- Lautstärke: `setVolume`/`setMuted` clampen 0..1, speichern sofort in `config.json`, wenden Boost-Faktor (`+10dB`) an.
```java
public static void setVolume(double volume) {
    globalVolume = Math.max(0, Math.min(1, volume));
    Game.getInstance().getConfig().getObject().put("soundVolume", volume);
    Game.getInstance().getConfig().save();
    applyVolume(backgroundPlayer);
}
```
- Assets: `assets/audio/...` (Effekte) + `Music.MENU` als Loop im MainMenu. `preloadAll()` lädt alles async.
- Prompt-Idee: Soundslider 0-100%, Mute-Checkbox, Test-Sound-Button, Speichern direkt gegen Config.

## 7. Netzwerk & Daten (Code + Erklärung)
- Client baut TCP auf `localhost:25570`, schickt Login-Paket, startet Receiver/Sender Threads.
```java
socket = new Socket(InetAddress.getByName("localhost"), 25570);
sendPacket(new ClientLoginPacket(thePlayer.getUuid()));
networkExecutor.execute(new ReceiverTask());
networkExecutor.execute(new SenderTask());
```
- Heartbeats: alle 50ms `ClientDataPacket` (20 Ticks/s) => Position/Status; Receiver deserialisiert Pakete (max 0.5 MB) und feuert `ReceivePacketEvent` über Event-System.
- Score: `sendFinalScore(long score)` verpackt Highscore in `ClientSubmitScorePacket`.
- Config enthält MySQL-Block für Server-Hooks; Client speichert Settings lokal in `config.json`.
- Sicherheit: Socket-/Stream-Closes im Fehlerfall, Executor wird beendet.

## 8. Debugging & offene Punkte (Code + Erklärung)
- Laufzeit-Toggles: F1 Tooltips/Quest-Bar, F2 Debug-Bar (FPS, onGround, Camera, NoClip/God), F3 NoClip+GodMode.
- Game Over: bei 0 HP oder aus Welt fallen -> zurück ins MainMenu (Guard gegen Mehrfach-Trigger).
- Pause: Overlay mit Weiter/Zum Menü, toggled per Button oben links.
- Offene Punkte: echtes Pausen-Flag blockt Physik nur teilweise; Camera-Bounds rechts/unten fehlen; Debug-Bar zeigt nur erste Zeile; kein echter Level-Fortschritts-Save außer `tutorialFinished`.
- Tests: keine automatisierten Tests vorhanden (Input/Physik/Netzwerk manuell prüfen).

## 9. Roadmap
- Kurzfristig: Kamera rechts/unten clampen, Pause-Flag in Update früh abbrechen, Debug-Bar auf 2-3 Zeilen, Laser/GasBarrier im Layout feinjustieren.
- Mittelfristig: Level-Editor + JSON-Load, echte Stats im LevelFinish (Zeit/Deaths/Files), Achievements an echte Kriterien koppeln, Multiplayer-Menü mit Serverliste.
- Langfristig: persistente Highscores/Achievements über Backend, Koop/Versus-Mode, bessere Assets/Animationen (Player/Enemy/Laser), Savegames pro Slot.
