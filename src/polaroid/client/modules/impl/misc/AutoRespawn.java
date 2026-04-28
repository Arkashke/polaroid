package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.DeathScreen;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;

@ModuleSystem(name = "AutoRespawn", type = Category.Misc, server = ServerCategory.NO, description = "Автоматически возрождает игрока после смерти")
public class AutoRespawn extends Module {

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (mc.currentScreen instanceof DeathScreen && mc.player.deathTime > 0) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }
}


