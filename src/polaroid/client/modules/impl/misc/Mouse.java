package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "Mouse", type = Category.Misc, server = ServerCategory.NO, description = "Настройки мыши")
public class Mouse extends Module {

    private final SliderSetting sensitivity = new SliderSetting("Чувствительность", 1.0f, 0.1f, 2.0f, 0.1f);
    private final BooleanSetting invertY = new BooleanSetting("Инвертировать Y", false);
    private final BooleanSetting smoothCamera = new BooleanSetting("Плавная камера", false);

    private double originalSensitivity;

    public Mouse() {
        addSettings(sensitivity, invertY, smoothCamera);
    }

    @Override
    public boolean onEnable() {
        if (mc.gameSettings != null) {
            originalSensitivity = mc.gameSettings.mouseSensitivity;
        }
        return super.onEnable();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.gameSettings == null) return;

        mc.gameSettings.mouseSensitivity = originalSensitivity * sensitivity.get();
        mc.gameSettings.smoothCamera = smoothCamera.get();
    }

    @Override
    public boolean onDisable() {
        if (mc.gameSettings != null) {
            mc.gameSettings.mouseSensitivity = originalSensitivity;
            mc.gameSettings.smoothCamera = false;
        }
        return super.onDisable();
    }

    public boolean shouldInvertY() {
        return isState() && invertY.get();
    }
}


