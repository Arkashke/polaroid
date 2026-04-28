package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import net.minecraft.network.play.client.CPlayerPacket;

@ModuleSystem(name = "NoFall", type = Category.Movement, server = ServerCategory.NO, description = "Убирает урон от падения")
public class NoFall extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Packet", "Packet", "Vanilla");

    public NoFall() {
        addSettings(mode);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (mode.is("Packet")) {
            if (mc.player.fallDistance > 3.0f) {
                mc.player.connection.sendPacket(new CPlayerPacket(true));
                mc.player.fallDistance = 0;
            }
        } else if (mode.is("Vanilla")) {
            mc.player.fallDistance = 0;
        }
    }
}


