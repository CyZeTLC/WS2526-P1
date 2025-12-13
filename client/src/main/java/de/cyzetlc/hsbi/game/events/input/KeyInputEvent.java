package de.cyzetlc.hsbi.game.events.input;

import de.cyzetlc.hsbi.game.event.Event;
import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.Setter;

/**
 * The {@code KeyInputEvent} is an event fired whenever a keyboard key state changes
 * (pressed or released) or its current pressed state is polled.
 * <p>
 * This event contains information about which key was involved and the nature of the
 * input action (e.g., PRESS, RELEASE).
 *
 * @see Event
 * @see InputType
 *
 * @author Tom Coombs
 */
@Getter @Setter
public class KeyInputEvent extends Event {
    private KeyCode keyCode;
    private InputType inputType;
}

