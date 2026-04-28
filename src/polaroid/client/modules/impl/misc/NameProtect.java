package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.Polaroid;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.StringSetting;
import net.minecraft.client.Minecraft;

@ModuleSystem(name = "StreamerMode", type = Category.Misc, server = ServerCategory.NO, description = "Скрывает ваш ник")
public class NameProtect extends Module {

    public static String fakeName = "";

    public StringSetting name = new StringSetting(
            "Заменяемое Имя",
            "t.me/polaroid",
            "Тут введи свой ник"
    );

    public NameProtect() {
        addSettings(name);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        fakeName = name.get();
    }
    
    @Override
    public boolean onEnable() {
        fakeName = name.get();
        return super.onEnable();
    }

    public static String getReplaced(String input) {
        if (Polaroid.getInstance() != null && Polaroid.getInstance().getFunctionRegistry().getNameProtect().isState()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), fakeName);
        }
        return input;
    }
}


