package polaroid.client.utils.rotation.impl;

import polaroid.client.utils.rotation.Rotation;
import polaroid.client.utils.rotation.RotationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Intave ротация - адаптированная под сервер Intave
 */
public class IntaveRotation extends RotationHandler {
    
    private long rotationStartTime = System.currentTimeMillis();
    private float noiseFactor = 1.0f;
    
    public IntaveRotation() {
        super("Intave");
    }
    
    @Override
    public Rotation limitAngleChange(Rotation current, Rotation target, Vector3d targetVec, net.minecraft.entity.Entity entity) {
        // Intave ротация с плавным движением
        float targetYaw = target.getYaw();
        float targetPitch = target.getPitch();
        
        float currentYaw = current.getYaw();
        float currentPitch = current.getPitch();
        
        // Вычисляем разницу углов
        float yawDiff = getAngleDifference(currentYaw, targetYaw);
        float pitchDiff = getAngleDifference(currentPitch, targetPitch);
        
        // Динамическая скорость в зависимости от расстояния до цели
        float yawSpeed = Math.max((90.0f - Math.abs(yawDiff)) / 40.0f, random(1.0, 5.0)) * random(0.9, 1.1);
        float pitchSpeed = Math.abs(pitchDiff) / 30.0f * random(0.9, 1.1);
        
        // Применяем скорость
        float yawChange = Math.signum(yawDiff) * Math.min(Math.abs(yawDiff), yawSpeed * 25.0f);
        float pitchChange = Math.signum(pitchDiff) * Math.min(Math.abs(pitchDiff), pitchSpeed * 25.0f);
        
        float newYaw = currentYaw + yawChange;
        float newPitch = MathHelper.clamp(currentPitch + pitchChange, -90.0f, 90.0f);
        
        return new Rotation(newYaw, newPitch);
    }
    
    private float getAngleDifference(float current, float target) {
        float diff = target - current;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return diff;
    }
    
    private float random(double min, double max) {
        return (float)(min + (max - min) * Math.random());
    }
    
    @Override
    public Vector3d randomValue() {
        return new Vector3d(0, 0, 0);
    }
}


