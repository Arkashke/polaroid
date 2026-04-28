//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import polaroid.client.events.EventMotion;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.utils.player.MouseUtil;

@ModuleSystem(
        name = "WaterSpeed FT",
        type = Category.Movement,
        server = ServerCategory.NO,
        description = "Ускоряет вас в воде"
)
public class WaterSpeed extends Module {
    @Subscribe
    public void onUpdate(EventMotion e) {
        Minecraft var10000 = mc;
        if (Minecraft.player.isSwimming()) {
            float var10001 = e.getYaw();
            float var10002 = e.getPitch();
            Minecraft var10003 = mc;
            BlockRayTraceResult r = (BlockRayTraceResult)MouseUtil.rayTrace((double)0.0F, var10001, var10002, Minecraft.player);
            Minecraft var7 = mc;
            Minecraft var13 = mc;
            mc.playerController.processRightClickBlock(Minecraft.player, Minecraft.world, Hand.MAIN_HAND, r);
            var7 = mc;
            if (this.hasDepthStrider(Minecraft.player)) {
                var10000 = mc;
                ClientPlayerEntity var4 = Minecraft.player;
                var7 = mc;
                double var10 = Minecraft.player.getMotion().x * 1.025;
                var13 = mc;
                var10003 = mc;
                var4.setVelocity(var10, Minecraft.player.getMotion().y, Minecraft.player.getMotion().z * 1.025);
            } else {
                var10000 = mc;
                ClientPlayerEntity var6 = Minecraft.player;
                var7 = mc;
                double var12 = Minecraft.player.getMotion().x * 1.015;
                var13 = mc;
                var10003 = mc;
                var6.setVelocity(var12, Minecraft.player.getMotion().y, Minecraft.player.getMotion().z * 1.015);
            }
        }

    }

    private boolean hasDepthStrider(PlayerEntity player) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, player.getItemStackFromSlot(EquipmentSlotType.FEET)) > 0;
    }
}


