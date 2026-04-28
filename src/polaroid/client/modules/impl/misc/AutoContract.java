package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.StopWatch;

@ModuleSystem(name = "AutoContract", type = Category.Misc, server = ServerCategory.NO, description = "Автоматически принимает контракты")
public class AutoContract extends Module {

    private final SliderSetting delay = new SliderSetting("Задержка (сек)", 5, 1, 30, 1);
    private final StopWatch stopWatch = new StopWatch();

    public AutoContract() {
        addSettings(delay);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (stopWatch.isReached((long) (delay.get() * 1000))) {
            // Логика принятия контракта (зависит от сервера)
            mc.player.sendChatMessage("/contract accept");
            stopWatch.reset();
        }
    }

    @Override
    public boolean onDisable() {
        stopWatch.reset();
        return super.onDisable();
    }
}


