package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "DragonFly", type = Category.Movement, server = ServerCategory.NO, description = "Полет как дракон")
public class DragonFly extends Module {

    private final SliderSetting speed = new SliderSetting("Скорость", 1.0f, 0.1f, 5.0f, 0.1f);
    private final SliderSetting verticalSpeed = new SliderSetting("Вертикальная скорость", 0.5f, 0.1f, 2.0f, 0.1f);

    public DragonFly() {
        addSettings(speed, verticalSpeed);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        mc.player.abilities.isFlying = true;
        mc.player.abilities.setFlySpeed(speed.get() / 10f);

        double motionY = 0;
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            motionY = verticalSpeed.get();
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            motionY = -verticalSpeed.get();
        }
        
        mc.player.setMotion(mc.player.getMotion().x, motionY, mc.player.getMotion().z);
    }

    @Override
    public boolean onDisable() {
        if (mc.player != null) {
            mc.player.abilities.isFlying = false;
            mc.player.abilities.setFlySpeed(0.05f);
        }
        return super.onDisable();
    }
}


