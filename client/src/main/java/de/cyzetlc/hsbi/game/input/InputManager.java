package de.cyzetlc.hsbi.game.input;

import de.cyzetlc.hsbi.game.events.input.InputType;
import de.cyzetlc.hsbi.game.events.input.KeyInputEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Der {@code InputManager} verwaltet den Zustand aller Tastatur- und Mauseingaben des Spiels.
 *
 * <p>
 * Er verfolgt, welche Tasten aktuell gedrückt sind ({@code pressedKeys}) und welche Tasten
 * *genau in diesem Frame* gedrückt wurden ({@code justPressedKeys}), um eine korrekte
 * Handhabung von einmaligen Aktionen (Entprellen/Polling) zu ermöglichen.
 * Außerdem registriert er Listener auf der JavaFX {@code Scene} und verteilt Eingabeereignisse.
 *
 * @author Tom Coombs
 * @author leonardo (aka. Phantomic)
 */
public class InputManager {
    /**
     * Menge der Tasten, die derzeit (über mehrere Frames hinweg) gedrückt gehalten werden.
     */
    private final Set<KeyCode> pressedKeys = new HashSet<>();
    /**
     * Menge der Tasten, die *neu* in diesem Frame gedrückt wurden (für einmalige Aktionen).
     */
    private final Set<KeyCode> justPressedKeys = new HashSet<>();

    /**
     * Die aktuelle X-Koordinate des Mauszeigers (relativ zur Scene).
     */
    @Getter
    private double mouseX = 0;

    /**
     * Die aktuelle Y-Koordinate des Mauszeigers (relativ zur Scene).
     */
    @Getter
    private double mouseY = 0;

    /**
     * Registriert alle notwendigen Event-Handler auf der gegebenen JavaFX {@code Scene},
     * um Tasten- und Mausereignisse zu verfolgen.
     *
     * @param scene Die Haupt-Scene des Spiels, auf der die Eingabe-Listener registriert werden.
     */
    public void register(Scene scene) {
        KeyInputEvent keyInputEvent = new KeyInputEvent();

        scene.setOnKeyPressed(event -> {
            if (!pressedKeys.contains(event.getCode())) {
                justPressedKeys.add(event.getCode()); // merken, dass Taste in diesem Frame neu gedrückt wurde
            }
            pressedKeys.add(event.getCode());

            // Ereignis verteilen
            keyInputEvent.setInputType(InputType.PRESSED);
            keyInputEvent.setKeyCode(event.getCode());
            keyInputEvent.call();
        });
        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
            justPressedKeys.remove(event.getCode()); // Sicherstellen, dass "justPressed" auch entfernt wird, falls der Frame übersprungen wurde

            // Ereignis verteilen
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

    /**
     * Prüft, ob die angegebene Taste aktuell gedrückt gehalten wird.
     *
     * @param key Der {@code KeyCode} der zu prüfenden Taste.
     * @return {@code true}, wenn die Taste gedrückt gehalten wird, andernfalls {@code false}.
     */
    public boolean isPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    /**
     * Liefert {@code true} genau im Frame des Tastendrucks (entprellt).
     * Danach wird der Status entfernt, sodass die Taste nicht mehrfach toggelt.
     *
     * @param key Der {@code KeyCode} der zu prüfenden Taste.
     * @return {@code true}, wenn die Taste genau in diesem Frame gedrückt wurde, andernfalls {@code false}.
     */
    public boolean pollJustPressed(KeyCode key) {
        boolean hit = justPressedKeys.contains(key);
        justPressedKeys.remove(key); // Status löschen, damit es im nächsten Frame nicht wieder True liefert
        return hit;
    }
}