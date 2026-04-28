package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.player.InventoryUtil;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TextFormatting;

@ModuleSystem(name = "ClanUpgrade", type = Category.Misc, server = ServerCategory.NO, description = "Автоматически прокачивает клан")
public class ClanUpgrade extends Module {
    
    private final StopWatch stopWatch = new StopWatch();

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;
        
        // Проверка на лобби
        if (isInLobby() && stopWatch.isReached(5000)) {
            print("В этом мире нельзя " + TextFormatting.RED + "прокачивать " + TextFormatting.RESET + "клан");
            stopWatch.reset();
            return;
        }
        
        // Поиск факела или редстоуна в хотбаре
        int slotId = findTorchOrRedstone();
        if (slotId == -1) {
            if (stopWatch.isReached(5000)) {
                print("Нужен " + TextFormatting.RED + "факел/редстоун " + TextFormatting.RESET + "в хотбаре");
                stopWatch.reset();
            }
            return;
        }
        
        // Переключение на нужный слот
        if (mc.player.inventory.currentItem != slotId) {
            mc.player.inventory.currentItem = slotId;
            return;
        }
        
        // Проверка позиции и установка/разрушение блока
        BlockPos pos = mc.player.getPosition().down();
        if (mc.world.getBlockState(pos).isSolid()) {
            if (mc.player.rotationPitch >= 89) {
                // Установка блока
                BlockRayTraceResult hitResult = new BlockRayTraceResult(
                    mc.player.getPositionVec(), 
                    Direction.UP, 
                    pos, 
                    false
                );
                mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, hitResult));
                
                // Разрушение блока
                mc.player.connection.sendPacket(new CPlayerDiggingPacket(
                    CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, 
                    pos.up(), 
                    Direction.UP
                ));
            }
        }
    }
    
    /**
     * Проверяет, находится ли игрок в лобби
     */
    private boolean isInLobby() {
        if (mc.world == null) return false;
        // Простая проверка - можно улучшить в зависимости от сервера
        String worldName = mc.world.getDimensionKey().getLocation().getPath();
        return worldName.contains("lobby") || worldName.contains("hub");
    }
    
    /**
     * Ищет факел или редстоун в хотбаре
     */
    private int findTorchOrRedstone() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TORCH ||
                mc.player.inventory.getStackInSlot(i).getItem() == Items.REDSTONE) {
                return i;
            }
        }
        return -1;
    }
}


