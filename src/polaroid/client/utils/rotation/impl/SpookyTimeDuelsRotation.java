package polaroid.client.utils.rotation.impl;

import polaroid.client.Polaroid;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.utils.math.SensUtils;
import polaroid.client.utils.rotation.Rotation;
import polaroid.client.utils.rotation.RotationHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class SpookyTimeDuelsRotation extends RotationHandler {

    public SpookyTimeDuelsRotation() {
        super("SpookyTimeDuels");
    }

    @Override
    public Vector3d randomValue() {
        float jitterSpeed = 3.8F;
        float jitterYaw = (float) ((Math.random() - 0.5) * jitterSpeed);
        float jitterPitch = (float) ((Math.random() - 0.5) * jitterSpeed);
        return new Vector3d(jitterYaw, jitterPitch, 0);
    }

    @Override
    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity target) {
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        
        if (target == null || mc.player == null || !(target instanceof LivingEntity)) {
            return currentRotation;
        }

        float yawToTarget = targetRotation.getYaw();
        float pitchToTarget = targetRotation.getPitch();

        // Проверка готовности к удару
        float attackStrength = mc.player.getCooledAttackStrength(0.5f);
        boolean readyToStrike = attackStrength >= 0.95f;

        // Рассчитываем разницу углов
        float diffYaw = MathHelper.wrapDegrees(yawToTarget - currentRotation.getYaw());
        float diffPitch = pitchToTarget - currentRotation.getPitch();

        // --- АДАПТИВНАЯ СКОРОСТЬ ---
        // Увеличиваем скорость, если ты или цель быстро двигаетесь (дистанция поворота растет)
        float distance = Math.abs(diffYaw) + Math.abs(diffPitch);

        // Базовая плавность (чем выше, тем быстрее доводка)
        float baseSmooth = 0.55f;

        // Если ты бегаешь вокруг (угол меняется сильно), ускоряем наводку
        float smoothFactor = (distance > 30.0f) ? 0.80f : baseSmooth;

        // Лимиты скорости за тик
        float maxYawSpeed = 65.0f;
        float maxPitchSpeed = 45.0f;

        // Рассчитываем шаги с учетом сглаживания
        float yawStep = MathHelper.clamp(diffYaw * smoothFactor, -maxYawSpeed, maxYawSpeed);
        float pitchStep = MathHelper.clamp(diffPitch * smoothFactor, -maxPitchSpeed, maxPitchSpeed);

        float targetY = currentRotation.getYaw() + yawStep;
        float targetP = currentRotation.getPitch() + pitchStep;

        // --- ПОСТОЯННЫЙ ДЖИТТЕР (Эффект "живой руки") ---
        // jitterSpeed 3.5f - 5.0f дает заметную тряску, которая сбивает античит
        Vector3d jitter = randomValue();
        targetY += (float) jitter.x;
        targetP += (float) jitter.y;

        // --- GCD FIX (Обязательно для плавности в движке MC) ---
        float gcdValue = SensUtils.getGCDValue();
        targetY -= (targetY - currentRotation.getYaw()) % gcdValue;
        targetP -= (targetP - currentRotation.getPitch()) % gcdValue;

        // Зажим вертикального угла
        targetP = MathHelper.clamp(targetP, -89.0f, 89.0f);

        // --- ФИКСАЦИЯ НА ТУЛОВИЩЕ ---
        if (aura.getCorrectionType().is("Фокусированная")) {
            mc.player.rotationYawOffset = targetY;
            mc.player.renderYawOffset = targetY;
            mc.player.rotationYawHead = targetY;
            mc.player.prevRotationYawHead = targetY;
            // Ключевой момент: Голова ВСЕГДА (и в прыжке) залочена на Pitch ротации
            mc.player.rotationPitchHead = targetP;
        }

        return new Rotation(targetY, targetP);
    }
}


