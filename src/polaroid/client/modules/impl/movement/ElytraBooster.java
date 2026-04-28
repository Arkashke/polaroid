package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.Polaroid;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.modules.impl.misc.ElytraHelper;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

@ModuleSystem(
        name = "ElytraBooster",
        type = Category.Movement,
        server = ServerCategory.NO,
        description = "Ускоряет полёт на элитрах"
)
public class ElytraBooster extends Module {
    
    public final ModeSetting mode = new ModeSetting("Режим", "Кастомный", "Кастомный", "Bravo", "ReallyWorld");

    public final BooleanSetting angleBasedSpeed = new BooleanSetting("Скорость по Углам", false);
    
    // Простые настройки скорости
    public final SliderSetting speedXZ = new SliderSetting("Скорость XZ", 1.65f, 1.5f, 2.5f, 0.01f)
            .setVisible(() -> mode.is("Кастомный") && !angleBasedSpeed.get());
    public final SliderSetting speedY = new SliderSetting("Скорость Y", 1.59f, 1.5f, 2.5f, 0.01f)
            .setVisible(() -> mode.is("Кастомный") && !angleBasedSpeed.get());

    // Настройки скорости по углам Yaw
    private final SliderSetting[] yawSpeeds = new SliderSetting[9];
    
    // Настройки скорости по углам Pitch
    private final SliderSetting[] pitchSpeeds = new SliderSetting[9];

    public ElytraBooster() {
        String[] angleRanges = {"0-5", "5-10", "10-15", "15-20", "20-25", "25-30", "30-35", "35-40", "40-45"};
        float[] defaultYawSpeeds = {1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f};
        float[] defaultPitchSpeeds = {1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f};
        
        // Создаем настройки для yaw
        for (int i = 0; i < yawSpeeds.length; i++) {
            final int index = i;
            yawSpeeds[i] = new SliderSetting("Yaw " + angleRanges[i], defaultYawSpeeds[i], 1.5f, 2.5f, 0.01f)
                    .setVisible(() -> mode.is("Кастомный") && angleBasedSpeed.get());
        }
        
        // Создаем настройки для pitch
        for (int i = 0; i < pitchSpeeds.length; i++) {
            final int index = i;
            pitchSpeeds[i] = new SliderSetting("Pitch " + angleRanges[i], defaultPitchSpeeds[i], 1.5f, 2.5f, 0.01f)
                    .setVisible(() -> mode.is("Кастомный") && angleBasedSpeed.get());
        }
        
        // Добавляем все настройки
        addSettings(mode, angleBasedSpeed, speedXZ, speedY);
        for (SliderSetting setting : yawSpeeds) {
            addSettings(setting);
        }
        for (SliderSetting setting : pitchSpeeds) {
            addSettings(setting);
        }
    }

    // Кэш для углов - избегаем повторных вычислений
    private float cachedYaw = 0;
    private float cachedPitch = 0;
    private float cachedSpeedX = 1.65f;
    private float cachedSpeedY = 1.59f;
    private int cacheUpdateCounter = 0;

    @Subscribe
    private void onUpdate(EventUpdate event) {
        // Оптимизация: ранний выход если не летим
        if (mc.player == null || !mc.player.isElytraFlying()) return;
        
        float yaw = mc.player.rotationYaw;
        float pitch = mc.player.rotationPitch;
        
        // Оптимизация: обновляем кэш только каждые 3 тика
        if (cacheUpdateCounter++ >= 3 || Math.abs(yaw - cachedYaw) > 5 || Math.abs(pitch - cachedPitch) > 5) {
            cacheUpdateCounter = 0;
            cachedYaw = yaw;
            cachedPitch = pitch;
            
            yaw = MathHelper.wrapDegrees(yaw);
            pitch = Math.abs(pitch);
            
            // Конвертируем углы в диапазон 0-45
            yaw = convertAngleToRange(yaw);
            pitch = convertAngleToRange(pitch);
            
            cachedSpeedX = getSpeedForYaw(yaw);
            cachedSpeedY = getSpeedForPitch(pitch);
            
            // Используем максимальную скорость
            if (cachedSpeedY > cachedSpeedX) {
                cachedSpeedX = cachedSpeedY;
            }
        }
        
        float speedX = cachedSpeedX;
        float speedYValue = cachedSpeedY;
        
        // Специальная логика для атаки
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        ElytraTarget elytraTarget = Polaroid.getInstance().getFunctionRegistry().getElytraTarget();
        
        if (aura != null && aura.isState() && elytraTarget != null && elytraTarget.isState()) {
            LivingEntity target = aura.getTarget();
            
            if (target != null && !target.isElytraFlying()) {
                speedX = 1.52f;
                speedYValue = 1.52f;
                
                // Если смотрим почти вертикально вниз
                if (mc.player.rotationPitch > 89.0f && mc.player.rotationPitch < 90.0f) {
                    speedYValue = 2.3f;
                }
            }
        }
        
        // Применяем скорость к движению
        Vector3d motion = mc.player.getMotion();
        mc.player.setMotion(motion.x * speedX, motion.y * speedYValue, motion.z * speedX);
    }
    
    private float getSpeedForYaw(float angle) {
        if (!angleBasedSpeed.get()) {
            return speedXZ.get();
        }
        
        int index = (int)(angle / 5.0f);
        if (index >= yawSpeeds.length) {
            index = yawSpeeds.length - 1;
        }
        return yawSpeeds[index].get();
    }
    
    private float getSpeedForPitch(float angle) {
        if (!angleBasedSpeed.get()) {
            return speedY.get();
        }
        
        int index = (int)(angle / 5.0f);
        if (index >= pitchSpeeds.length) {
            index = pitchSpeeds.length - 1;
        }
        return pitchSpeeds[index].get();
    }
    
    private float convertAngleToRange(float angle) {
        float absAngle = Math.abs(angle);
        
        // Конвертируем 0-180 в 0-90
        if (absAngle > 90.0f) {
            absAngle = 180.0f - absAngle;
        }
        
        // Конвертируем 0-90 в 0-45
        if (absAngle > 45.0f) {
            absAngle = 90.0f - absAngle;
        }
        
        return absAngle;
    }
}


