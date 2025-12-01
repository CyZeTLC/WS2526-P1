# Steal The Files - Documentation (EN)
Client | Server | Core - JavaFX 2D platformer

**Contents**
1. Overview & Architecture
2. Setup & Start
3. Game Logic (code + notes)
4. UI/Navigation (code + notes + prompt)
5. Level Build (code + notes)
6. Audio & Settings (code + notes + prompt)
7. Networking & Data (code + notes)
8. Debugging & Open Points (code + notes)
9. Roadmap

## 1. Overview & Architecture
Level selection + first screen; LoadingScreen always first, then MainMenu.
```java
switch (config.getString("currentLevel")) {
    case "Tutorial" -> setCurrentLevel(new TutorialLevel());
    case "Second"   -> setCurrentLevel(new SecondLevel());
    case "Boss"     -> setCurrentLevel(new BossLevel());
}
screenManager.showScreen(new LoadingScreen(screenManager));
```
Logic thread + JavaFX render keeps UI responsive; delta comes from the loop.
```java
while (running) {
    double delta = (now - lastTime) / 1_000_000_000.0;
    lastTime = now;
    Platform.runLater(() -> currentScreen.update(delta));
    Thread.sleep(5);
}
```

## 2. Setup & Start
Build/run via Maven; start Server and Client from IDE.
```bash
mvn clean install
```
Client init applies sound from config, builds screens, shows LoadingScreen.
```java
SoundManager.setMuted(config.getBoolean("soundMuted"));
SoundManager.setVolume(config.getDouble("soundVolume"));
screenManager = new ScreenManager(primaryStage);
mainMenuScreen = new MainMenuScreen(screenManager);
settingsScreen = new SettingsScreen(screenManager);
screenManager.showScreen(new LoadingScreen(screenManager));
```
Key config: `currentLevel`, `soundVolume`, `soundMuted`, `messages.game.prefix`, `mysql`, `tutorialFinished`.

## 3. Game Logic (code + notes)
Debounced toggles for HUD/Debug/NoClip/GodMode.
```java
boolean f1 = input.pollJustPressed(KeyCode.F1);
boolean f2 = input.pollJustPressed(KeyCode.F2);
boolean f3 = input.pollJustPressed(KeyCode.F3);
if (f1) showTooltips = !showTooltips;
if (f2) showDebugBar = !showDebugBar;
if (f3) { player.setNoClip(!player.isNoClipEnabled()); player.setGodMode(player.isNoClipEnabled()); }
```
Delta-based physics + bounding box for collisions.
```java
dy += Game.gravity * delta;
double nextX = x + dx;
double nextY = y + dy;
Rectangle2D next = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());
```
Platform collision resolves landing vs. side to zero-out velocity.
```java
if (next.intersects(pBounds)) {
    if (y + player.getHeight() <= platform.getY()) { nextY = platform.getY() - player.getHeight(); dy = 0; onGround = true; }
    else if (x + player.getWidth() <= platform.getX()) { nextX = platform.getX() - player.getWidth(); dx = 0; }
}
```
Barrier needs flipper + E; otherwise lethal.
```java
if (block instanceof GasBarrierBlock barrier && interactPressed && player.hasFlipper()) {
    barrier.deactivate();
    continue;
}
```
Enemy firing uses range + cooldown; lasers expire after 4s (in LaserBlock).
```java
boolean close = Math.abs(player.getLocation().getX() - getLocation().getX()) < 440;
if (close && fireTimer >= fireCooldown) {
    fireTimer = 0;
    return new LaserBlock(new Location(spawnX, eyeY), dir, 320);
}
```
Perk reset restores defaults after 10s.
```java
Game.jumpPower *= 1.25;
PauseTransition t = new PauseTransition(Duration.seconds(10));
t.setOnFinished(e -> Game.jumpPower = 800);
t.play();
```
Progress gate: USB enables folder collection; folders deactivate when picked.
```java
if (block instanceof USBStickBlock) Game.thePlayer.setCanCollectFiles(true);
if (block instanceof FolderBlock && Game.thePlayer.isCanCollectFiles()) block.setActive(false);
```
Camera smoothing via simple lerp keeps motion stable.
```java
double targetX = player.getLocation().getX() - marginX;
cameraX += (targetX - cameraX) * cameraSmooth;
```
Game over check aborts frame and returns to main menu.
```java
if (player.getHealth() <= 0) { handleGameOver(); return; }
```

## 4. UI/Navigation (code + notes + prompt)
LoadingScreen: 3s timeline for progress; warms textures in parallel; jumps to MainMenu on finish.
```java
KeyFrame kf = new KeyFrame(Duration.millis(3000), kvWidth);
timeline.getKeyFrames().add(kf);
new Thread(() -> { for (Material m : Material.values()) cache(m); }).start();
timeline.setOnFinished(e -> showScreen(mainMenu));
```
SettingsScreen: live volume; mute toggles slider; both persisted.
```java
volumeSlider.valueProperty().addListener((o, ov, nv) -> { SoundManager.setVolume(nv.doubleValue()); updateVolumeLabel(); });
muteBtn.setOnAction(e -> {
    SoundManager.setMuted(!SoundManager.isMuted());
    volumeSlider.setDisable(SoundManager.isMuted());
    updateMuteButton();
});
```
HUD refresh: files from active folders, hearts from player health.
```java
totalFolderCount = countFolderBlocks();
filesProgressLbl.setText("Files: " + countActiveFolders() + "/" + totalFolderCount);
updateHealth();
```
Pause overlay only flips visibility (logic still runs).
```java
private void togglePause() {
    paused = !paused;
    if (pauseOverlay != null) pauseOverlay.setVisible(paused);
}
```
Fullscreen stage without OS chrome for immersion.
```java
stage.initStyle(StageStyle.UNDECORATED);
stage.setFullScreen(true);
stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
```
**Prompt (Camera, ChatGPT)**: "Give me a simple dead-zone camera for a 2D Java/JavaFX platformer that lerps smoothly to the player; suggest marginX/marginY and a smoothing factor." Result: Dead zone marginX=400, marginY=150, smoothing=0.1 using the lerp shown above.

## 5. Level Build (code + notes)
Lava helper auto-fills gaps between platforms.
```java
for (int i = 0; i < ordered.size()-1; i++) {
    double gapStart = cur.getX() + cur.getWidth();
    double gapWidth = next.getX() - gapStart;
    if (gapWidth > 1.0) blocks.add(createLavaColumn(gapStart, lavaY, gapWidth, lavaH));
}
```
Tutorial highlights (USB gate, floating platform, finish right).
```java
platforms.add(new Platform(0, h-300, 450, 600, root));
blocks.add(new USBStickBlock(new Location(200, h-360)));
blocks.add(new FloatingPlatformBlock(new Location(940, h-340), new Location(1224, h-340), 120));
blocks.add(new FinishBlock(new Location(2400, h-490)));
```
SecondLevel highlights (flipper early, boss robot end, gas barrier prepped).
```java
blocks.add(new FlipperItem(new Location(220, h-320)));
blocks.add(new RobotEnemyBlock(new Location(5000, h-396), 500, 180));
// blocks.add(new GasBarrierBlock(new Location(4550, h-428), 64, 128));
blocks.add(new FolderBlock(new Location(3600, h-530)));
```
BossLevel (lane of robots + finish).
```java
blocks.add(new RobotEnemyBlock(new Location(250, h-396), 500, 180));
blocks.add(new RobotEnemyBlock(new Location(1400, h-396), 500, 180));
blocks.add(new FinishBlock(new Location(1800, h-390)));
```
Notes: USB before folders enforces progression; coords hardcoded; no camera bounds; `Level.update()` empty.

## 6. Audio & Settings (code + notes + prompt)
Persist volume/mute immediately to config; UI reflects state.
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
Ducking + menu music: SFX can duck music briefly; menu loop runs in background.
```java
SoundManager.playWithDuck(Sound.JUMP_BOOST, 1.0, 0.06);
SoundManager.playBackground(Music.MENU, true);
```
Settings UI disables slider when muted.
```java
volumeSlider.setDisable(SoundManager.isMuted());
```
**Prompt (Audio, ChatGPT)**: "We use JavaFX MediaPlayer for music/SFX. How much dB boost is safe as a global gain, and how far should we duck music during SFX?" Result: ~+10 dB soft boost via factor, duck music to ~6% base volume (see `DB_BOOST` and `playWithDuck`).

## 7. Networking & Data (code + notes)
Client connect/login and start I/O threads.
```java
socket = new Socket(InetAddress.getByName("localhost"), 25570);
sendPacket(new ClientLoginPacket(thePlayer.getUuid()));
networkExecutor.execute(new ReceiverTask());
networkExecutor.execute(new SenderTask());
```
Heartbeats every 50ms keep connection alive.
```java
while (!socket.isClosed()) {
    sendPacket(new ClientDataPacket(thePlayer.getUuid()));
    Thread.sleep(50);
}
```
Server deserializes; events can cancel processing.
```java
int bytesRead = dis.read(received);
Packet packet = SerializationUtils.deserialize(actual, Packet.class);
if (((EventCancelable) new ReceivePacketEvent(packet, socket).call()).isCancelled()) break;
```
Send score (level name fixed).
```java
sendPacket(new ClientSubmitScorePacket(thePlayer.getUuid(), score, "Level_1"));
```

## 8. Debugging & Open Points (code + notes)
Runtime toggles and game-over guard each frame.
```java
if (f1) showTooltips = !showTooltips;
if (f2) showDebugBar = !showDebugBar;
if (f3) { player.setNoClip(!player.isNoClipEnabled()); player.setGodMode(player.isNoClipEnabled()); }
if (player.getHealth() <= 0) { handleGameOver(); return; }
```
Open: pause overlay only; `Level.update()` empty; camera clamps missing; gas barrier commented; LevelFinished stats placeholder.

## 9. Roadmap
Short term: real pause, real LevelFinished stats, camera bounds, enable GasBarrier path.
Mid term: use `Level.update()` for movers/timers; externalize levels (JSON/editor); HUD timer/score.
Long term: complete multiplayer/highscore DB flow; backend-driven achievements/server list.
