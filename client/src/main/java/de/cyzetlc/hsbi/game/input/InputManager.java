package de.cyzetlc.hsbi.game.input;

import de.cyzetlc.hsbi.game.events.input.InputType;
import de.cyzetlc.hsbi.game.events.input.KeyInputEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class InputManager {
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Set<KeyCode> justPressedKeys = new HashSet<>();
    @Getter

    private double mouseX = 0;

    @Getter
    private double mouseY = 0;

    public void register(Scene scene) {
        KeyInputEvent keyInputEvent = new KeyInputEvent();

        scene.setOnKeyPressed(event -> {
            if (!pressedKeys.contains(event.getCode())) {
                justPressedKeys.add(event.getCode()); // merken, dass Taste in diesem Frame neu gedrückt wurde
            }
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

        scene.setOnMouseMoved(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });
        scene.setOnMouseDragged(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });
    }

    public boolean isPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    /**
     * Liefert true genau im Frame des Tastendrucks (entprellt).
     * Danach wird der Status entfernt, sodass die Taste nicht mehrfach toggelt.
     */
    public boolean pollJustPressed(KeyCode key) {
        boolean hit = justPressedKeys.contains(key);
        justPressedKeys.remove(key);
        return hit;
    }
}
