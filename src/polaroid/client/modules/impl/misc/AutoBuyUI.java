package polaroid.client.modules.impl.misc;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BindSetting;

@ModuleSystem(name = "AutoBuy(OLD)", type = Category.Misc, server = ServerCategory.NO, description = "Меню авто бая для FunTime(OLD)")
public class AutoBuyUI extends Module {

    public BindSetting setting = new BindSetting("Кнопка открытия", -1);

    public AutoBuyUI() {
        addSettings(setting);
    }
}


