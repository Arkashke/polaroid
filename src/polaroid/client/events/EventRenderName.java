package polaroid.client.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.LivingEntity;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class EventRenderName extends CancelEvent {
    private LivingEntity entity;
}


