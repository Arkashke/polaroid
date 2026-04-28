package polaroid.client.events;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoSlowEvent extends CancelEvent {
    Type type;

    public NoSlowEvent(Type type) {
        this.type = type;
    }

    public boolean isPost() {
        return type == Type.POST;
    }

    public enum Type {
        PRE, POST
    }
}


