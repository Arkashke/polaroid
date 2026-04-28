package polaroid.client.modules.impl.player;


import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import net.minecraftforge.eventbus.api.Event;


@ModuleSystem(name = "CustomModels", type = Category.Render, server = ServerCategory.NO, description = "Меняет модель игрока")
public class CustomModels extends Module {

    public final ModeSetting models = new ModeSetting("Модель", "Crazy Rabbit", "Crazy Rabbit","Freddy Bear","White Demon", "Red Demon");
    public final BooleanSetting friends = new BooleanSetting("Применять на друзей",true);

    public CustomModels() {
        addSettings(models, friends);
    }

    public boolean onEvent(Event event) {
        return false;
    }
}

