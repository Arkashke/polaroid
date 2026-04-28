package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.StopWatch;

@ModuleSystem(name = "Tape", type = Category.Misc, server = ServerCategory.NO, description = "Автоматически повторяет действия")
public class Tape extends Module {

    private final BooleanSetting autoWalk = new BooleanSetting("Авто ходьба", true);
    private final BooleanSetting autoJump = new BooleanSetting("Авто прыжок", false);
    private final BooleanSetting autoAttack = new BooleanSetting("Авто атака", false);
    private final SliderSetting attackDelay = new SliderSetting("Задержка атаки (мс)", 500, 100, 2000, 50)
            .setVisible(() -> autoAttack.get());

    private final StopWatch attackTimer = new StopWatch();

    public Tape() {
        addSettings(autoWalk, autoJump, autoAttack, attackDelay);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (autoWalk.get()) {
            mc.gameSettings.keyBindForward.setPressed(true);
        }

        if (autoJump.get() && mc.player.isOnGround()) {
            mc.player.jump();
        }

        if (autoAttack.get() && attackTimer.isReached(attackDelay.get().longValue())) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.getType() != null) {
                mc.gameSettings.keyBindAttack.setPressed(true);
                attackTimer.reset();
            }
        }
    }

    @Override
    public boolean onDisable() {
        if (mc.player != null && mc.gameSettings != null) {
            mc.gameSettings.keyBindForward.setPressed(false);
            mc.gameSettings.keyBindAttack.setPressed(false);
        }
        attackTimer.reset();
        return super.onDisable();
    }
}


