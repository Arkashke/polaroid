package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.StopWatch;

@ModuleSystem(name = "AutoDuel", type = Category.Misc, server = ServerCategory.NO, description = "Автоматически принимает дуэли")
public class AutoDuel extends Module {

    private final SliderSetting delay = new SliderSetting("Задержка (сек)", 3, 1, 10, 1);
    private final StopWatch stopWatch = new StopWatch();

    public AutoDuel() {
        addSettings(delay);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (stopWatch.isReached((long) (delay.get() * 1000))) {
            // Логика принятия дуэли (зависит от сервера)
            // Обычно это команда /duel accept или клик по предмету
            mc.player.sendChatMessage("/duel accept");
            stopWatch.reset();
        }
    }

    @Override
    public boolean onDisable() {
        stopWatch.reset();
        return super.onDisable();
    }
}


