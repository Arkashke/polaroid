package polaroid.client.modules.impl.render;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.components.SliderComponent;

@ModuleSystem(name = "ClickGui", type = Category.Render, server = ServerCategory.NO, description = "Меню клиента")
public class ClickGui extends Module {
    public ModeSetting alphaMode = new ModeSetting("Режим прозрачности", "Прозрачный", "Прозрачный", "Статичный");
    public SliderSetting rect = new SliderSetting("Скругление ректов", 3,0,6,1);
    public SliderSetting rect2 = new SliderSetting("Скругление ClickGui", 3,0,12,1);
    public SliderSetting scale = new SliderSetting("Размер ClickGui", 1.0f, 0.5f, 2.0f, 0.05f);
    public BooleanSetting outline = new BooleanSetting("Обводка ректов", true);
    public BooleanSetting circle = new BooleanSetting("Кружок модулей", true);

    public ClickGui() {
        addSettings(scale);
    }
}

