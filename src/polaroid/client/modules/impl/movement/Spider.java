package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "Spider", type = Category.Movement, server = ServerCategory.NO, description = "Позволяет лазить по стенам")
public class Spider extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Motion", "Motion", "Jump");
    private final SliderSetting speed = new SliderSetting("Скорость", 0.2f, 0.1f, 1.0f, 0.05f);

    public Spider() {
        addSettings(mode, speed);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (mc.player.collidedHorizontally) {
            if (mode.is("Motion")) {
                mc.player.setMotion(
                    mc.player.getMotion().x,
                    speed.get(),
                    mc.player.getMotion().z
                );
            } else if (mode.is("Jump")) {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
        }
    }
}


