package de.cyzetlc.hsbi.game.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Event {
    private final Logger logger = LoggerFactory.getLogger(Event.class.getName());

    public Event call() {
        final List<EventData> dataList = EventManager.get(this.getClass());

        if (dataList != null) {
            for (EventData data : dataList) {
                try {
                    data.target.invoke(data.source, this);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return this;
    }
}
