package polaroid.client.modules.impl.misc;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import net.minecraft.util.text.TextFormatting;

import java.util.Random;

@ModuleSystem(name = "RussianRoulette", type = Category.Misc, server = ServerCategory.NO, description = "50/50 шанс крашнуть игру")
public class RussianRoulette extends Module {
    
    private final Random random = new Random();
    private boolean hasPlayed = false;

    @Override
    public boolean onEnable() {
        if (mc.player == null) {
            toggle();
            return false;
        }

        if (!hasPlayed) {
            playRoulette();
            hasPlayed = true;
        }

        // Автоматически выключаем модуль после использования
        toggle();
        return false;
    }

    private void playRoulette() {
        // 50/50 шанс
        boolean lucky = random.nextBoolean();

        if (lucky) {
            // Повезло!
            mc.player.sendMessage(
                new net.minecraft.util.text.StringTextComponent(TextFormatting.GREEN + "А ты хуев везунчик"), 
                net.minecraft.util.Util.DUMMY_UUID
            );
            
            // Звук победы
            mc.player.playSound(net.minecraft.util.SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            // Не повезло - крашим игру
            mc.player.sendMessage(
                new net.minecraft.util.text.StringTextComponent(TextFormatting.RED + "Не повезло..."), 
                net.minecraft.util.Util.DUMMY_UUID
            );
            
            // Крашим игру через несколько способов для надежности
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Способ 1: Выход из игры
                System.exit(0);
                
                // Способ 2: Если первый не сработал, выбрасываем критическую ошибку
                throw new RuntimeException("Russian Roulette: BOOM! 💥");
            }).start();
        }
    }

    @Override
    public boolean onDisable() {
        hasPlayed = false;
        super.onDisable();
        return false;
    }
}


