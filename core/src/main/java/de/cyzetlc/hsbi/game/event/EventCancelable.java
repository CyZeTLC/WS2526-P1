package de.cyzetlc.hsbi.game.event;

import lombok.Getter;
import lombok.Setter;

public class EventCancelable extends Event {
    @Getter @Setter
    private boolean cancelled = false;
}
