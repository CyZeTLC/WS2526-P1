package de.cyzetlc.hsbi.game.events;

import de.cyzetlc.hsbi.game.event.Event;
import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class KeyInputEvent extends Event {
    private KeyCode keyCode;
    private InputType inputType;
}

