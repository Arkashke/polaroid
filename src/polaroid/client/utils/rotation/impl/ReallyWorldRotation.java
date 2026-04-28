package polaroid.client.utils.rotation.impl;

import polaroid.client.Polaroid;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.rotation.Rotation;
import polaroid.client.utils.rotation.RotationHandler;
import polaroid.client.utils.rotation.RotationMath;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.security.SecureRandom;

public class ReallyWorldRotation extends RotationHandler {
    
    private final StopWatch attackTimer = new StopWatch();
    private int attackCount = 0;

    public ReallyWorldRotation() {
        super("ReallyWorld");
    }

    @Override
    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity entity) {
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        
        if (entity != null) {
            Vector3d aimPoint = RotationMath.getTargetPoint(entity, 0.33f, 1.0f);
            targetRotation = RotationMath.calculateAngle(aimPoint);
            
            // Новая логика ротации
            boolean shouldAttack = mc.player.getCooledAttackStrength(0.5f) >= 0.92f;
            
            if (shouldAttack) {
                attackCount++;
            }
            
            float yawToTarget = targetRotation.getYaw();
            float pitchToTarget = targetRotation.getPitch();
            float yawDelta = MathHelper.wrapDegrees(yawToTarget - currentRotation.getYaw());
            float pitchDelta = MathHelper.wrapDegrees(pitchToTarget - currentRotation.getPitch());
            
            float clampedYaw = Math.max(Math.abs(yawDelta), 0.0f);
            float clampedPitch = Math.max(Math.abs(pitchDelta), 0.0f);
            
            float yaw = currentRotation.getYaw() + (yawDelta > 0 ? clampedYaw : -clampedYaw);
            float pitch = MathHelper.clamp(currentRotation.getPitch() + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);
            
            // GCD коррекция
            float gcd = getGCDValue();
            yaw -= (yaw - currentRotation.getYaw()) % gcd;
            pitch -= (pitch - currentRotation.getPitch()) % gcd;
            
            return new Rotation(yaw, pitch);
        }
        
        return currentRotation;
    }
    
    private float getGCDValue() {
        float f = (float) (mc.gameSettings.mouseSensitivity * 0.6F + 0.2F);
        return f * f * f * 1.2F;
    }

    @Override
    public Vector3d randomValue() {
        return new Vector3d(0, 0, 0);
    }
}


