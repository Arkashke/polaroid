package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;

@ModuleSystem(name = "NoJumpDelay", type = Category.Movement, server = ServerCategory.NO, description = "Отключает задержку на прыжок")
public class NoJumpDelay extends Module {
    @Subscribe
    public void onUpdate(EventUpdate e) {
        mc.player.jumpTicks = 0;
    }
}


