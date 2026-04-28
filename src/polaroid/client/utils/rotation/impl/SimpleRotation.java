package polaroid.client.utils.rotation.impl;

import polaroid.client.utils.rotation.Rotation;
import polaroid.client.utils.rotation.RotationHandler;
import polaroid.client.utils.rotation.RotationMath;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Простая ротация - базовая система наведения
 */
public class SimpleRotation extends RotationHandler {

    public SimpleRotation() {
        super("Simple");
    }

    @Override
    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity entity) {
        if (entity instanceof LivingEntity) {
            return targetRotation;
        }
        return currentRotation;
    }

    @Override
    public Vector3d randomValue() {
        return new Vector3d(0, 0, 0);
    }
}



