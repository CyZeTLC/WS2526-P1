package de.cyzetlc.hsbi.game.input;

import de.cyzetlc.hsbi.game.events.input.InputType;
import de.cyzetlc.hsbi.game.events.input.KeyInputEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

public class InputManager {
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public void register(Scene scene) {
        KeyInputEvent keyInputEvent = new KeyInputEvent();

        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            keyInputEvent.setInputType(InputType.PRESSED);
            keyInputEvent.setKeyCode(event.getCode());
            keyInputEvent.call();
        });
        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());

            keyInputEvent.setInputType(InputType.RELEASED);
            keyInputEvent.setKeyCode(event.getCode());
            keyInputEvent.call();
        });
    }

    public boolean isPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }
}
