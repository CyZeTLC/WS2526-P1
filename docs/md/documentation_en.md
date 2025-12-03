# Steal The Files - Documentation (EN)
JavaFX 2D platformer (Client/Server/Core)

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
- Entry: `Game` boots JavaFX, loads `config.json`, applies sound, connects TCP client, instantiates `EntityPlayer` + current `Level`.
- `ScreenManager`: scene controller with its own logic thread (~200 Hz) + JavaFX render thread; fullscreen window, custom CSS.
- Event layer (`EventManager`): routes network/input events to listeners (Key/Packet/UserMessage/Player).
- Level choice is only wired here via `config.currentLevel`.
```java
switch (config.getString("currentLevel")) {
    case "Tutorial" -> setCurrentLevel(new TutorialLevel());
    case "Second"   -> setCurrentLevel(new SecondLevel());
    case "Boss"     -> setCurrentLevel(new BossLevel());
}
```
- Global gameplay params: `gravity=15`, `moveSpeed=450 px/s`, `jumpPower=800` (consumed by physics).

## 2. Setup & Start
- Build: `mvn clean install` (Client/Server/Core), requires Java 17 + JavaFX.
- Run: start Server and Client from IDE or `java -jar target/...` (assets live in `client/src/main/resources`).
- Config (`config.json`): `currentLevel`, `soundVolume`/`soundMuted`, `messages.game.prefix`, `mysql.*`, `tutorialFinished`.
- Client boot: reads sound settings, builds `ScreenManager`, shows `LoadingScreen`, jumps into `MainMenu`, starts background music.
```java
SoundManager.setMuted(config.getBoolean("soundMuted"));
SoundManager.setVolume(config.getDouble("soundVolume"));
screenManager = new ScreenManager(primaryStage);
screenManager.showScreen(new LoadingScreen(screenManager));
```

## 3. Game Logic (code + notes)
- Input debounce: `InputManager.pollJustPressed` keeps F1/F2/F3 toggles single-fire per frame.
```java
boolean f1 = input.pollJustPressed(KeyCode.F1);
boolean f2 = input.pollJustPressed(KeyCode.F2);
boolean f3 = input.pollJustPressed(KeyCode.F3);
if (f1) showTooltips = !showTooltips;
if (f2) showDebugBar = !showDebugBar;
if (f3) { player.setNoClip(!player.isNoClipEnabled()); player.setGodMode(player.isNoClipEnabled()); }
```
- Movement/physics (frame-independent): velocity from `delta`, gravity, jump, A/D for walking; NoClip skips gravity + collisions.
```java
dy += Game.gravity * delta;
double nextX = x + dx;
double nextY = y + dy;
Rectangle2D next = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());
```
- Platform collision: separates landing vs side/ceiling, also applies moving block delta.
```java
if (next.intersects(pBounds)) {
    if (y + player.getHeight() <= platform.getY()) { nextY = platform.getY() - player.getHeight(); dy = 0; onGround = true; }
    else if (x + player.getWidth() <= platform.getX()) { nextX = platform.getX() - player.getWidth(); dx = 0; }
}
```
- Blocks & enemies: `GasBarrier` kills without Flipper+E, `RobotEnemy` fires `LaserBlock` (4s lifetime), stomp-kill from above, `FolderBlock` counts files (only after USB).
```java
if (block instanceof GasBarrierBlock barrier && interactPressed && player.hasFlipper()) { barrier.deactivate(); continue; }
if (block instanceof RobotEnemyBlock enemy) { LaserBlock laser = enemy.tryFire(player); if (laser != null) pendingBlocks.add(laser); }
```
- Camera smoothing: dead zone + Lerp per axis; clamps disabled while NoClip is on.
```java
double targetX = cameraX + (playerScreenX > width - marginX ? playerScreenX - (width - marginX) :
                           playerScreenX < marginX ? -(marginX - playerScreenX) : 0);
cameraX += (targetX - cameraX) * cameraSmooth;
```
- HUD: hearts (top right), quest/file progress, tooltip bar; `togglePause()` shows overlay with Continue/Menu.

## 4. UI/Navigation (code + notes + prompt)
- Screens: `LoadingScreen` (prewarm assets), `MainMenu` (scrolling background, system/achievement mock), `Settings`, `Multiplayer`, `GameScreen`, `LevelFinished`.
- ScreenManager: fullscreen, `assets/style.css`, input registration + worker-thread game loop, render via `Platform.runLater`.
- MainMenu animation: two background layers scroll endlessly.
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
- Buttons: `UIUtils.drawCenteredButton` for main CTAs; GameScreen shows Back/Pause in the top-left.
- Prompt/tooltips: F1 shows green help text, F2 Debug Bar, F3 NoClip+God.

## 5. Level Build (code + notes)
- Base `Level`: owns `platforms` + `blocks`, tracks `levelStarted`, optional `nextLevel`.
- Lava autofill: `placeLavaBetweenPlatforms(sceneHeight)` sorts by X and fills gaps with `LavaBlock` columns.
```java
ordered.sort(Comparator.comparingDouble(Platform::getX));
if (gapWidth > 1.0) blocks.add(createLavaColumn(gapStart, lavaY, gapWidth, lavaH));
```
- Tutorial: intro layout, USB stick gates file collection; jump + speed buffs, floating platform, finish on the right.
- Second: higher jumps, Flipper item before gas barrier, RobotEnemy boss at the end, multiple folder collectibles.
- Boss: long floor, four RobotEnemies, finish shortly after (combat focused).

## 6. Audio & Settings (code + notes + prompt)
- `SoundManager`: caches media, plays SFX concurrently, `playWithDuck` temporarily ducks music, background music via `playBackground` (loop optional).
- Volume: `setVolume`/`setMuted` clamp 0..1, persist immediately to `config.json`, applies a +10dB boost factor.
```java
public static void setVolume(double volume) {
    globalVolume = Math.max(0, Math.min(1, volume));
    Game.getInstance().getConfig().getObject().put("soundVolume", volume);
    Game.getInstance().getConfig().save();
    applyVolume(backgroundPlayer);
}
```
- Assets: `assets/audio/...` (effects) + `Music.MENU` loop in MainMenu; `preloadAll()` warms everything async.
- Prompt idea: slider 0-100%, mute checkbox, "play test sound" button, save straight to config.

## 7. Networking & Data (code + notes)
- Client opens TCP to `localhost:25570`, sends login packet, starts receiver/sender threads.
```java
socket = new Socket(InetAddress.getByName("localhost"), 25570);
sendPacket(new ClientLoginPacket(thePlayer.getUuid()));
networkExecutor.execute(new ReceiverTask());
networkExecutor.execute(new SenderTask());
```
- Heartbeats: every 50ms a `ClientDataPacket` (20 ticks/s); receiver deserializes up to 0.5 MB and fires `ReceivePacketEvent` through the event system.
- Score submit: `sendFinalScore(long score)` wraps a `ClientSubmitScorePacket`.
- Config carries a MySQL block for backend hooks; client stores settings locally in `config.json`.
- Safety: closes socket/streams on failure, shuts down executor.

## 8. Debugging & Open Points (code + notes)
- Runtime toggles: F1 tooltips/quest bar, F2 debug bar (FPS, onGround, camera, NoClip/God), F3 NoClip+GodMode.
- Game over: 0 HP or falling out of the world -> back to MainMenu (guard against double trigger).
- Pause: overlay with Continue/Menu, toggled via top-left button.
- Gaps: pause flag only partially stops logic; camera clamp missing on right/bottom; debug bar only shows first line; no real level progress save except `tutorialFinished`.
- Tests: no automated coverage (input/physics/network are manual).

## 9. Roadmap
- Short term: clamp camera on right/bottom, enforce pause flag early in update, multi-line debug bar, fine-tune laser/gas barrier placements.
- Mid term: level editor + JSON load, real stats in LevelFinish (time/deaths/files), achievements with real criteria, multiplayer menu with server list.
- Long term: persistent highscores/achievements via backend, co-op/versus mode, better assets/animations (player/enemy/laser), savegames per slot.
