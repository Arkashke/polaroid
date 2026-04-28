package polaroid.client.scripts.lua.classes.events;

import lombok.Getter;

public class EventClass {
    @Getter
    private String eventName;

    public EventClass(String eventName) {
        this.eventName = eventName;
    }
    
}


