package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "VelocitySync", type = Category.Movement, server = ServerCategory.NO, description = "Синхронизирует скорость движения")
public class VelocitySync extends Module {

    private final SliderSetting multiplier = new SliderSetting("Множитель", 1.0f, 0.1f, 2.0f, 0.1f);

    public VelocitySync() {
        addSettings(multiplier);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isOnGround() && (mc.player.moveForward != 0 || mc.player.moveStrafing != 0)) {
            float mult = multiplier.get();
            mc.player.setMotion(
                mc.player.getMotion().x * mult,
                mc.player.getMotion().y,
                mc.player.getMotion().z * mult
            );
        }
    }
}


