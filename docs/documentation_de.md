# Steal The Files - Dokumentation (DE)
Client | Server | Core - JavaFX 2D-Plattformer

**Inhalt**
1. &Uuml;berblick &amp; Architektur
2. Setup &amp; Start
3. Spiellogik (Code + Erkl&auml;rung)
4. GUI/Navi (Code + Erkl&auml;rung + Prompt)
5. Levelaufbau (Code + Erkl&auml;rung)
6. Audio &amp; Einstellungen (Code + Erkl&auml;rung + Prompt)
7. Netzwerk &amp; Daten (Code + Erkl&auml;rung)
8. Debugging &amp; offene Punkte (Code + Erkl&auml;rung)
9. Roadmap

## 1. &Uuml;berblick &amp; Architektur
Levelwahl + Startscreen: Config bestimmt Einstieg; erst LoadingScreen, dann MainMenu. Der Switch ist die einzige Stelle, die das Startlevel setzt.
```java
switch (config.getString("currentLevel")) {
    case "Tutorial" -> setCurrentLevel(new TutorialLevel());
    case "Second"   -> setCurrentLevel(new SecondLevel());
    case "Boss"     -> setCurrentLevel(new BossLevel());
}
screenManager.showScreen(new LoadingScreen(screenManager));
```
Game-Loop: Logik-Takt im eigenen Thread (ca. 200 Hz); Render/Scene-&Auml;nderungen laufen auf dem JavaFX-Thread, damit UI nicht blockiert.
```java
while (running) {
    double delta = (now - lastTime) / 1_000_000_000.0;
    lastTime = now;
    Platform.runLater(() -> currentScreen.update(delta));
    Thread.sleep(5);
}
```

## 2. Setup &amp; Start
Build/Run: klassischer Maven-Build; danach Server + Client aus der IDE starten.
```bash
mvn clean install
```
Client-Initialisierung: liest Sound-Einstellungen, baut ScreenManager + Hauptscreens und zeigt sofort den LoadingScreen. Alles wird direkt aus `config.json` &uuml;bernommen.
```java
SoundManager.setMuted(config.getBoolean("soundMuted"));
SoundManager.setVolume(config.getDouble("soundVolume"));
screenManager = new ScreenManager(primaryStage);
mainMenuScreen = new MainMenuScreen(screenManager);
settingsScreen = new SettingsScreen(screenManager);
screenManager.showScreen(new LoadingScreen(screenManager));
```
Wichtige Config-Schl&uuml;ssel: `currentLevel` (Start), `soundVolume`/`soundMuted`, `messages.game.prefix`, `mysql` (Server-Hooks), `tutorialFinished` (Fortschritt).

## 3. Spiellogik (Code + Erkl&auml;rung)
Input-Toggles: `pollJustPressed` verhindert mehrfaches Ausl&ouml;sen im selben Frame; F1/F2/F3 steuern HUD/Debug/NoClip+GodMode.
```java
boolean f1 = input.pollJustPressed(KeyCode.F1);
boolean f2 = input.pollJustPressed(KeyCode.F2);
boolean f3 = input.pollJustPressed(KeyCode.F3);
if (f1) showTooltips = !showTooltips;
if (f2) showDebugBar = !showDebugBar;
if (f3) { player.setNoClip(!player.isNoClipEnabled()); player.setGodMode(player.isNoClipEnabled()); }
```
Physik-Schritt: nutzt delta, damit Geschwindigkeit unabh&auml;ngig von FPS bleibt; Bounding-Box dient als Basis f&uuml;r Kollisionen.
```java
dy += Game.gravity * delta;
double nextX = x + dx;
double nextY = y + dy;
Rectangle2D next = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());
```
Plattform-Kollision: trennt Landung (oben) von Seitenkontakt; setzt Velocity passend zur&uuml;ck.
```java
if (next.intersects(pBounds)) {
    if (y + player.getHeight() <= platform.getY()) { nextY = platform.getY() - player.getHeight(); dy = 0; onGround = true; }
    else if (x + player.getWidth() <= platform.getX()) { nextX = platform.getX() - player.getWidth(); dx = 0; }
}
```
GasBarrier: erfordert Flipper-Flag + Tastendruck E, sonst t&ouml;dlich.
```java
if (block instanceof GasBarrierBlock barrier && interactPressed && player.hasFlipper()) {
    barrier.deactivate();
    continue;
}
```
Laser-Spawn: pr&uuml;ft horizontalen Abstand und Cooldown, erzeugt Projektil mit fixer Lifetime (4s im LaserBlock).
```java
boolean close = Math.abs(player.getLocation().getX() - getLocation().getX()) < 440;
if (close && fireTimer >= fireCooldown) {
    fireTimer = 0;
    return new LaserBlock(new Location(spawnX, eyeY), dir, 320);
}
```
Perk-Reset: Buffs (Jump/Speed) laufen 10s, danach werden Standardwerte gesetzt.
```java
Game.jumpPower *= 1.25;
PauseTransition t = new PauseTransition(Duration.seconds(10));
t.setOnFinished(e -> Game.jumpPower = 800);
t.play();
```
Progress-Gate: USB schaltet Sammeln frei, Folder z&auml;hlen erst danach.
```java
if (block instanceof USBStickBlock) Game.thePlayer.setCanCollectFiles(true);
if (block instanceof FolderBlock && Game.thePlayer.isCanCollectFiles()) block.setActive(false);
```
Kamera-Smoothing: einfacher Lerp Richtung Ziel, um ruckelfreies Nachziehen zu erhalten.
```java
double targetX = player.getLocation().getX() - marginX;
cameraX += (targetX - cameraX) * cameraSmooth;
```
Game-Over: stoppt den Frame und wechselt ins Hauptmen&uuml;.
```java
if (player.getHealth() <= 0) { handleGameOver(); return; }
```

## 4. GUI/Navi (Code + Erkl&auml;rung + Prompt)
LoadingScreen: Fortschrittsbalken via Timeline, Assets werden parallel vorgew&auml;rmt; danach automatisch ins MainMenu.
```java
KeyFrame kf = new KeyFrame(Duration.millis(3000), kvWidth);
timeline.getKeyFrames().add(kf);
new Thread(() -> { for (Material m : Material.values()) cache(m); }).start();
timeline.setOnFinished(e -> showScreen(mainMenu));
```
SettingsScreen: Volume-Slider wirkt sofort, Mute deaktiviert den Slider; Werte landen direkt in der Config.
```java
volumeSlider.valueProperty().addListener((o, ov, nv) -> { SoundManager.setVolume(nv.doubleValue()); updateVolumeLabel(); });
muteBtn.setOnAction(e -> {
    SoundManager.setMuted(!SoundManager.isMuted());
    volumeSlider.setDisable(SoundManager.isMuted());
    updateMuteButton();
});
```
HUD: aktualisiert Files und Health pro Frame; nutzt aktive Folder-Bl&ouml;cke als Z&auml;hler.
```java
totalFolderCount = countFolderBlocks();
filesProgressLbl.setText("Files: " + countActiveFolders() + "/" + totalFolderCount);
updateHealth();
```
Pause: blendet nur Overlay ein, Logik l&auml;uft weiter (Debug).
```java
private void togglePause() {
    paused = !paused;
    if (pauseOverlay != null) pauseOverlay.setVisible(paused);
}
```
Stage: Fullscreen, ohne Window-Chrome, keine Exit-Kombo (Absicht: Immersion).
```java
stage.initStyle(StageStyle.UNDECORATED);
stage.setFullScreen(true);
stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
```
**Prompt (Kamera, ChatGPT)**: "Gib mir eine einfache Dead-Zone-Kamera f&uuml;r einen 2D-Plattformer in Java/JavaFX, die weich auf den Spieler lerpt; nenne marginX/marginY und einen smoothing-Faktor." Ergebnis: Dead-Zone marginX=400, marginY=150, smoothing=0.1 mit einfachem Lerp wie oben umgesetzt.

## 5. Levelaufbau (Code + Erkl&auml;rung)
Lava-Helper: schlie&szlig;t Gaps zwischen sortierten Plattformen automatisch mit Lava-S&auml;ulen.
```java
for (int i = 0; i < ordered.size()-1; i++) {
    double gapStart = cur.getX() + cur.getWidth();
    double gapWidth = next.getX() - gapStart;
    if (gapWidth > 1.0) blocks.add(createLavaColumn(gapStart, lavaY, gapWidth, lavaH));
}
```
TutorialLevel: Basis-Layout mit fr&uuml;hem USB-Gate, FloatingPlatform und Finish rechts.
```java
platforms.add(new Platform(0, h-300, 450, 600, root));
blocks.add(new USBStickBlock(new Location(200, h-360)));
blocks.add(new FloatingPlatformBlock(new Location(940, h-340), new Location(1224, h-340), 120));
blocks.add(new FinishBlock(new Location(2400, h-490)));
```
SecondLevel: Flipper nahe Start, RobotEnemy am Ende, GasBarrier vorbereitet (auskommentiert).
```java
blocks.add(new FlipperItem(new Location(220, h-320)));
blocks.add(new RobotEnemyBlock(new Location(5000, h-396), 500, 180));
// blocks.add(new GasBarrierBlock(new Location(4550, h-428), 64, 128));
blocks.add(new FolderBlock(new Location(3600, h-530)));
```
BossLevel: vier Roboter in Linie, Finish dahinter.
```java
blocks.add(new RobotEnemyBlock(new Location(250, h-396), 500, 180));
blocks.add(new RobotEnemyBlock(new Location(1400, h-396), 500, 180));
blocks.add(new FinishBlock(new Location(1800, h-390)));
```
Notizen: USB vor Foldern zwingt Progress; Koordinaten hart codiert, keine Kamera-Bounds, `Level.update()` leer.

## 6. Audio &amp; Einstellungen (Code + Erkl&auml;rung + Prompt)
Persistenz: Volume/Mute werden sofort in der Config gesichert; UI spiegelt den Zustand.
```java
public static void setVolume(double volume) {
    globalVolume = Math.max(0, Math.min(1, volume));
    Game.getInstance().getConfig().getObject().put("soundVolume", volume);
    Game.getInstance().getConfig().save();
}
public static void setMuted(boolean muted) {
    SoundManager.muted = muted;
    Game.getInstance().getConfig().getObject().put("soundMuted", muted);
    Game.getInstance().getConfig().save();
}
```
Ducking + Men&uuml;musik: Effekte k&ouml;nnen Musik kurz absenken, Men&uuml;-Loop l&auml;uft im Hintergrund.
```java
SoundManager.playWithDuck(Sound.JUMP_BOOST, 1.0, 0.06);
SoundManager.playBackground(Music.MENU, true);
```
UI-Feedback: Slider wird deaktiviert, wenn stumm.
```java
volumeSlider.setDisable(SoundManager.isMuted());
```
**Prompt (Audio, ChatGPT)**: "Wir haben JavaFX MediaPlayer f&uuml;r Musik/Effekte. Wie viel dB Gain k&ouml;nnen wir pauschal boosten, ohne zu clippen, und wie stark sollten wir die Musik beim Effekt ducking absenken?" Ergebnis: ca. +10 dB Soft-Boost &uuml;ber Faktor, Ducking auf etwa 6% Grundlautst&auml;rke (siehe `DB_BOOST` und `playWithDuck`).

## 7. Netzwerk &amp; Daten (Code + Erkl&auml;rung)
Client-Verbindung: TCP an localhost:25570, sendet Login, startet Receiver/Sender Threads.
```java
socket = new Socket(InetAddress.getByName("localhost"), 25570);
sendPacket(new ClientLoginPacket(thePlayer.getUuid()));
networkExecutor.execute(new ReceiverTask());
networkExecutor.execute(new SenderTask());
```
Heartbeats: 20 Ticks/s halten die Session aktiv.
```java
while (!socket.isClosed()) {
    sendPacket(new ClientDataPacket(thePlayer.getUuid()));
    Thread.sleep(50);
}
```
Server-Handling: deserialisiert Pakete, Events k&ouml;nnen Abbruch ausl&ouml;sen.
```java
int bytesRead = dis.read(received);
Packet packet = SerializationUtils.deserialize(actual, Packet.class);
if (((EventCancelable) new ReceivePacketEvent(packet, socket).call()).isCancelled()) break;
```
Score-Push: sendet Highscore (Level-Name derzeit fest).
```java
sendPacket(new ClientSubmitScorePacket(thePlayer.getUuid(), score, "Level_1"));
```

## 8. Debugging &amp; offene Punkte (Code + Erkl&auml;rung)
Runtime-Toggles + Game-Over: schaltet HUD/Debug/NoClip, beendet Frame bei 0 HP.
```java
if (f1) showTooltips = !showTooltips;
if (f2) showDebugBar = !showDebugBar;
if (f3) { player.setNoClip(!player.isNoClipEnabled()); player.setGodMode(player.isNoClipEnabled()); }
if (player.getHealth() <= 0) { handleGameOver(); return; }
```
Offen: Pause stoppt Logik nicht; `Level.update()` leer; Kamera-Clamps fehlen; GasBarrier im SecondLevel auskommentiert; LevelFinished-Stats Platzhalter.

## 9. Roadmap
Kurzfristig: echte Pause, echte LevelFinished-Stats, Kamera-Bounds, GasBarrier aktivieren.
Mittelfristig: `Level.update()` f&uuml;r mover/timer, Leveldaten aus JSON/Editor, HUD um Timer/Score.
Langfristig: Multiplayer/Highscore mit DB fertigstellen, Achievements/Serverliste ans Backend binden.
