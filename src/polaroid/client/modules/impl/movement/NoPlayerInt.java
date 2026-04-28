package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;

@ModuleSystem(name = "NoPlayerInt", type = Category.Movement, server = ServerCategory.NO, description = "Отключает взаимодействие с игроками")
public class NoPlayerInt extends Module {

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;
        
        // Отключаем коллизию с другими игроками
        for (net.minecraft.entity.Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof net.minecraft.entity.player.PlayerEntity && entity != mc.player) {
                entity.setNoGravity(false);
            }
        }
    }
}


