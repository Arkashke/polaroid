package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import net.minecraft.entity.player.PlayerEntity;
import polaroid.client.modules.api.ServerCategory;

@ModuleSystem(name = "SeeInvisibles", type = Category.Render, server = ServerCategory.NO, description = "Показывает людей, которые находятся в невидимости")
public class SeeInvisibles extends Module {


    @Subscribe
    private void onUpdate(EventUpdate e) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isInvisible()) {
                player.setInvisible(false);
            }
        }
    }

}


