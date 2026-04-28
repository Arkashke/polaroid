package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ModuleSystem(name = "SexAura", type = Category.Combat, server = ServerCategory.NO, description = "Подходит сзади к энтити и приседает")
public class SexAura extends Module {
    
    private final SliderSetting range = new SliderSetting("Дистанция", 10.0f, 3.0f, 20.0f, 0.5f);
    private final SliderSetting crouchSpeed = new SliderSetting("Скорость приседа", 5.0f, 1.0f, 20.0f, 1.0f);
    
    private LivingEntity target = null;
    private final StopWatch crouchTimer = new StopWatch();
    private boolean isCrouching = false;
    private int crouchCount = 0;
    private static final int MAX_CROUCHES = 10; // Количество приседаний

    public SexAura() {
        addSettings(range, crouchSpeed);
    }

    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        // Ищем ближайшую цель
        if (target == null || !target.isAlive() || mc.player.getDistance(target) > range.get()) {
            target = findTarget();
            crouchCount = 0;
        }

        if (target == null) {
            stopCrouching();
            return;
        }

        double distance = mc.player.getDistance(target);
        
        // Если близко к цели, приседаем
        if (distance <= 2.5) {
            performCrouching();
        } else {
            // Иначе двигаемся к цели
            stopCrouching();
            moveToTarget();
        }
    }

    private LivingEntity findTarget() {
        List<LivingEntity> entities = mc.world.getLoadedEntitiesWithinAABB(LivingEntity.class, 
            mc.player.getBoundingBox().grow(range.get()))
            .stream()
            .filter(entity -> entity != mc.player)
            .filter(Entity::isAlive)
            .sorted(Comparator.comparingDouble(entity -> mc.player.getDistance(entity)))
            .collect(Collectors.toList());

        return entities.isEmpty() ? null : entities.get(0);
    }

    private void moveToTarget() {
        if (target == null) return;

        // Просто двигаемся к цели
        Vector3d targetPos = target.getPositionVec();
        Vector3d playerPos = mc.player.getPositionVec();
        Vector3d direction = targetPos.subtract(playerPos).normalize();

        // Устанавливаем движение напрямую
        double speed = 0.25;
        mc.player.setMotion(direction.x * speed, mc.player.getMotion().y, direction.z * speed);

        // Поворачиваем взгляд на цель
        float[] rotations = getRotations(target);
        mc.player.rotationYaw = rotations[0];
        mc.player.rotationPitch = rotations[1];
    }

    private void performCrouching() {
        long crouchDelay = (long) (1000.0f / crouchSpeed.get());

        if (crouchTimer.isReached(crouchDelay)) {
            // Переключаем присед
            isCrouching = !isCrouching;
            
            // Используем прямое управление приседом
            if (isCrouching) {
                mc.gameSettings.keyBindSneak.setPressed(true);
            } else {
                mc.gameSettings.keyBindSneak.setPressed(false);
                crouchCount++;
            }

            // Если достигли максимума приседаний, ищем новую цель
            if (crouchCount >= MAX_CROUCHES) {
                target = null;
                crouchCount = 0;
                stopCrouching();
            }

            crouchTimer.reset();
        }
    }

    private void stopCrouching() {
        if (isCrouching) {
            mc.gameSettings.keyBindSneak.setPressed(false);
            isCrouching = false;
        }
    }

    private float[] getRotations(LivingEntity entity) {
        double x = entity.getPosX() - mc.player.getPosX();
        double y = entity.getPosY() + entity.getEyeHeight() - (mc.player.getPosY() + mc.player.getEyeHeight());
        double z = entity.getPosZ() - mc.player.getPosZ();

        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(y, dist) * 180.0 / Math.PI);

        return new float[]{yaw, pitch};
    }

    @Override
    public boolean onEnable() {
        target = null;
        stopCrouching();
        super.onEnable();
        return false;
    }

    @Override
    public boolean onDisable() {
        stopCrouching();
        target = null;
        super.onDisable();
        return false;
    }
}


