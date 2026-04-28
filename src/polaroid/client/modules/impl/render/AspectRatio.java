package polaroid.client.modules.impl.render;


import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "AspectRatio", type = Category.Render, server = ServerCategory.NO, description = "Изменяет размер экрана")
public class AspectRatio extends Module {
    public SliderSetting width = new SliderSetting("Ширина", 1, 0.6f, 2.3f, 0.1f);
    
    public AspectRatio() {
        addSettings(width);
    }
}

