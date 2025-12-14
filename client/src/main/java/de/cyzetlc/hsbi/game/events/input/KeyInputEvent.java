package de.cyzetlc.hsbi.game.events.input;

import de.cyzetlc.hsbi.game.event.Event;
import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Das {@code KeyInputEvent} ist ein Ereignis, das ausgelöst wird, wann immer sich der Zustand
 * einer Tastaturtaste ändert (gedrückt oder losgelassen) oder ihr aktueller gedrückter Zustand
 * abgefragt wird.
 * <p>
 * Dieses Ereignis enthält Informationen darüber, welche Taste beteiligt war und welche Art von
 * Eingabeaktion stattgefunden hat (z. B. PRESS, RELEASE).
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

