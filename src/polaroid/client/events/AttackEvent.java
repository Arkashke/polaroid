package polaroid.client.events;

import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;

@AllArgsConstructor
public class AttackEvent extends CancelEvent {
    public Entity entity;
}


