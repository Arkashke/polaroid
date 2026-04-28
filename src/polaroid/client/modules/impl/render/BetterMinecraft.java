package polaroid.client.modules.impl.render;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;

@ModuleSystem(name = "BetterMinecraft", type = Category.Misc, server = ServerCategory.NO, description = "Изменяет визуалы внутри майнкрафта")
public class BetterMinecraft extends Module {

    public final BooleanSetting smoothChat = new BooleanSetting("Плавный чат", true);
    public final BooleanSetting smoothTab = new BooleanSetting("Плавный таб", true);
    public final BooleanSetting betterTab = new BooleanSetting("Улучшенный таб", true);
    public final BooleanSetting betterChat = new BooleanSetting("Улучшенный чат", true);
    public final BooleanSetting smoothCamera = new BooleanSetting("Плавная камера", true);

    public BetterMinecraft() {
        addSettings(betterTab, betterChat, smoothChat, smoothCamera);
    }
}


