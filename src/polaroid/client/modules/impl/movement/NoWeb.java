package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

@ModuleSystem(name = "NoWeb", type = Category.Movement, server = ServerCategory.NO, description = "Убирает замедление от паутины")
public class NoWeb extends Module {

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getPosition();
        
        if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
            // Убираем замедление от паутины через изменение скорости
            mc.player.setMotion(mc.player.getMotion().mul(5.0, 1.0, 5.0));
            mc.player.fallDistance = 0;
        }
    }
}


