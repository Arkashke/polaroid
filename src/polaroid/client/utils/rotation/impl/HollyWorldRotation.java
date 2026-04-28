package polaroid.client.utils.rotation.impl;

import polaroid.client.Polaroid;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.utils.math.SensUtils;
import polaroid.client.utils.rotation.Rotation;
import polaroid.client.utils.rotation.RotationHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

public class HollyWorldRotation extends RotationHandler {

    public HollyWorldRotation() {
        super("HollyWorld");
    }

    @Override
    public Vector3d randomValue() {
        float jitterX = (float) ((Math.random() - 0.5) * 0.25);
        float jitterY = (float) ((Math.random() - 0.5) * 0.25);
        return new Vector3d(jitterX, jitterY, 0);
    }

    private void setServerRotation(float yaw, float pitch) {
        if (mc.player != null) {
            mc.player.rotationYawHead = yaw;
            mc.player.renderYawOffset = yaw;
        }
    }

    @Override
    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity target) {
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        
        if (target == null || mc.player == null || !(target instanceof LivingEntity)) {
            return currentRotation;
        }

        LivingEntity livingTarget = (LivingEntity) target;

        // Вычисляем расстояние до цели
        double hj_diffX = livingTarget.getPosX() - mc.player.getPosX();
        double hj_diffZ = livingTarget.getPosZ() - mc.player.getPosZ();
        double hj_dist = Math.sqrt(hj_diffX * hj_diffX + hj_diffZ * hj_diffZ);
        
        // Динамическая высота цели в зависимости от расстояния
        float hj_step = (float) MathHelper.clamp((hj_dist - 2.0) / 3.0, 0.0, 1.0);
        float hj_dynamicY = MathHelper.lerp(hj_step, 0.2F, livingTarget.getEyeHeight());
        float hj_serverTargetY = (float) (livingTarget.getPosY() + hj_dynamicY);
        
        // Вычисляем углы для серверной ротации
        double sj_dX = livingTarget.getPosX() - mc.player.getPosX();
        double sj_dZ = livingTarget.getPosZ() - mc.player.getPosZ();
        double sj_dY = hj_serverTargetY - (mc.player.getPosY() + mc.player.getEyeHeight());
        double sj_dst = Math.sqrt(sj_dX * sj_dX + sj_dZ * sj_dZ);
        
        float sj_Yaw = (float) (Math.toDegrees(Math.atan2(sj_dZ, sj_dX)) - 90.0);
        float sj_Pitch = (float) (-Math.toDegrees(Math.atan2(sj_dY, sj_dst)));
        
        // Добавляем небольшой джиттер
        Vector3d jitter = randomValue();
        sj_Yaw += (float) jitter.x;
        sj_Pitch += (float) jitter.y;
        
        // GCD коррекция
        float mouseSensitivity = (float) mc.gameSettings.mouseSensitivity;
        float fCalculation = mouseSensitivity * 0.6F + 0.2F;
        float hj_gcd = fCalculation * fCalculation * fCalculation * 1.2F;
        sj_Yaw -= (sj_Yaw - currentRotation.getYaw()) % hj_gcd;
        sj_Pitch -= (sj_Pitch - currentRotation.getPitch()) % hj_gcd;
        
        // Устанавливаем серверную ротацию при атаке
        boolean isAttacking = mc.player.getCooledAttackStrength(1.0F) >= 0.92F;
        if (isAttacking) {
            setServerRotation(sj_Yaw, sj_Pitch);
        }
        
        // Вычисляем плавную визуальную ротацию
        float hj_yawDiff = MathHelper.wrapDegrees(sj_Yaw - currentRotation.getYaw());
        float hj_pitchDiff = sj_Pitch - currentRotation.getPitch();
        
        // Ограничение скорости ротации
        float hj_rotLimit = Math.abs(hj_yawDiff) > 90.0F ? 15.0F : 35.0F;
        float hj_visualSlowdown = 0.4F;
        
        float hj_stepYaw = MathHelper.clamp(hj_yawDiff * hj_visualSlowdown, -hj_rotLimit, hj_rotLimit);
        float hj_stepPitch = hj_pitchDiff * hj_visualSlowdown;
        
        float finalYaw = currentRotation.getYaw() + hj_stepYaw;
        float finalPitch = MathHelper.clamp(currentRotation.getPitch() + hj_stepPitch, -90.0F, 90.0F);
        
        // Применяем коррекцию движения
        if (aura.getCorrectionType().is("Фокусированная")) {
            mc.player.rotationYawOffset = finalYaw;
        }
        
        return new Rotation(finalYaw, finalPitch);
    }
}


