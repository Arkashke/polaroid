package polaroid.client.events;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@Setter
public class MovingEvent {
    private Vector3d from, to, motion;
    private boolean toGround;
    private AxisAlignedBB aabbFrom;
    private boolean ignoreHorizontal, ignoreVertical, collidedHorizontal, collidedVertical;
    private boolean sprinting; // Для сброса спринта как в Assistent

    public MovingEvent(Vector3d from, Vector3d to, Vector3d motion, boolean toGround,
            boolean isCollidedHorizontal, boolean isCollidedVertical, AxisAlignedBB aabbFrom) {
        this.from = from;
        this.to = to;
        this.motion = motion;
        this.toGround = toGround;
        this.collidedHorizontal = isCollidedHorizontal;
        this.collidedVertical = isCollidedVertical;
        this.aabbFrom = aabbFrom;
        this.sprinting = true; // По умолчанию не сбрасываем
    }
}


