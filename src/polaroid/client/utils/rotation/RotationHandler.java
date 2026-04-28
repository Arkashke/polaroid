package polaroid.client.utils.rotation;

import polaroid.client.utils.client.IMinecraft;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@Setter
public abstract class RotationHandler implements IMinecraft {
    
    private final String name;

    public RotationHandler(String name) {
        this.name = name;
    }

    public abstract Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity entity);

    public abstract Vector3d randomValue();

    protected float randomLerp(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }
}


