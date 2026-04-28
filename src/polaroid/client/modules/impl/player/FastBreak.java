package polaroid.client.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "FastBreak", type = Category.Player, server = ServerCategory.NO, description = "Ускоряет ломание блоков")
public class FastBreak extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Packet", "Packet", "Damage");
    private final SliderSetting speed = new SliderSetting("Скорость", 1.5f, 1.0f, 3.0f, 0.1f);

    public FastBreak() {
        addSettings(mode, speed);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.playerController == null) return;

        if (mode.is("Damage")) {
            if (mc.playerController.curBlockDamageMP > 0) {
                mc.playerController.curBlockDamageMP += speed.get() - 1.0f;
            }
        }
    }

    public float getSpeedMultiplier() {
        return isState() && mode.is("Packet") ? speed.get() : 1.0f;
    }
}


