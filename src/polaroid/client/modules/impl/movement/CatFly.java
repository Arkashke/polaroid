package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "CatFly", type = Category.Movement, server = ServerCategory.NO, description = "Плавный полет как кошка")
public class CatFly extends Module {

    private final SliderSetting speed = new SliderSetting("Скорость", 1.0f, 0.1f, 3.0f, 0.1f);
    private final SliderSetting glideSpeed = new SliderSetting("Скорость планирования", 0.3f, 0.1f, 1.0f, 0.05f);

    public CatFly() {
        addSettings(speed, glideSpeed);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
            float speedValue = speed.get();
            mc.player.setMotion(
                mc.player.getMotion().x * speedValue,
                mc.player.getMotion().y,
                mc.player.getMotion().z * speedValue
            );
        }

        // Плавное падение
        if (mc.player.getMotion().y < 0) {
            mc.player.setMotion(
                mc.player.getMotion().x,
                mc.player.getMotion().y * glideSpeed.get(),
                mc.player.getMotion().z
            );
        }

        mc.player.fallDistance = 0;
    }
}


