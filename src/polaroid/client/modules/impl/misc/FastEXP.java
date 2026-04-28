package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventMotion;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import net.minecraft.item.Items;
import polaroid.client.modules.api.ServerCategory;

@ModuleSystem(name = "FastEXP", type = Category.Player, description = "Отключает задержку на использование пузырька опыта", server = ServerCategory.NO)
public class FastEXP extends Module {
    @Subscribe
    public void onEvent(EventMotion e) {
        if (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            mc.rightClickDelayTimer = 1;
        }
    }
}

