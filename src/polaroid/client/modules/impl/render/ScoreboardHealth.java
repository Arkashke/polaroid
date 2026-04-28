package polaroid.client.modules.impl.render;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.utils.player.RealHpUtility;
import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

@ModuleSystem(name = "ScoreBoardHP", type = Category.Render, server = ServerCategory.NO, description = "Отображает реальное HP игроков, обходя защиту сервера.")
public class ScoreboardHealth extends Module {

    private int tickCounter = 0;

    @Override
    public boolean onEnable() {
        super.onEnable();
        return false;
    }

    @Override
    public boolean onDisable() {
        super.onDisable();
        return false;
    }
    
    @Subscribe
    public void onUpdate(EventUpdate event) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.world == null || mc.player == null) {
            return;
        }
        
        // Обновляем HP всех игроков в мире
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof AbstractClientPlayerEntity && player != mc.player) {
                // Вызываем getRealHp для обновления трекера
                RealHpUtility.getRealHp((AbstractClientPlayerEntity) player);
            }
        }
        
        // Очищаем старые данные каждые 5 секунд (100 тиков)
        tickCounter++;
        if (tickCounter >= 100) {
            RealHpUtility.cleanupOldData();
            tickCounter = 0;
        }
    }
}


