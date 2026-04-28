package polaroid.client.modules.impl.movement;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;

@ModuleSystem(name = "Sprint", type = Category.Movement, server = ServerCategory.NO, description = "Автоматически бежит")
public class AutoSprint extends Module {
    public BooleanSetting saveSprint = new BooleanSetting("Удерживать всегда", true).setVisible(() -> funtime.is("Легит-Снап"));
    public static ModeSetting funtime = new ModeSetting("Тип Спринта", "Default", "Default","FunTime");
    public AutoSprint() {
        addSettings(saveSprint, funtime);
    }
}


