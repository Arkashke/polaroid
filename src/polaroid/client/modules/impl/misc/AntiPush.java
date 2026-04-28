package polaroid.client.modules.impl.misc;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import lombok.Getter;

@Getter
@ModuleSystem(name = "AntiPush", type = Category.Player, server = ServerCategory.NO, description = "Отключает отталкивание")
public class AntiPush extends Module {

    private final ModeListSetting modes = new ModeListSetting("Тип",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Вода", false),
            new BooleanSetting("Блоки", true));

    public AntiPush() {
        addSettings(modes);
    }

}


