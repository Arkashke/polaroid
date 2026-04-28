package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.AttackEvent;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "ShiftTap", type = Category.Combat, server = ServerCategory.NO, description = "Автоматически нажимает Shift при атаке")
public class ShiftTap extends Module {
    
    private long shiftTapEndTime = 0;
    private boolean isModuleControllingSneak = false;
    
    private final SliderSetting shiftTapDuration = new SliderSetting("Длительность", 25, 10, 100, 1);

    public ShiftTap() {
        addSettings(shiftTapDuration);
    }
    
    private void startShiftTap() {
        shiftTapEndTime = System.currentTimeMillis() + Math.round(shiftTapDuration.get());
        if (!isModuleControllingSneak) {
            mc.gameSettings.keyBindSneak.setPressed(true);
            isModuleControllingSneak = true;
        }
    }
    
    private void stopShiftTap() {
        if (isModuleControllingSneak) {
            mc.gameSettings.keyBindSneak.setPressed(false);
            isModuleControllingSneak = false;
        }
    }
    
    @Subscribe
    public void onAttack(AttackEvent event) {
        if (mc.player == null) {
            return;
        }
        startShiftTap();
    }
    
    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.player.isSpectator()) {
            stopShiftTap();
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (isModuleControllingSneak && currentTime > shiftTapEndTime) {
            stopShiftTap();
        }
    }
    
    @Override
    public boolean onDisable() {
        super.onDisable();
        stopShiftTap();
        return false;
    }
}


