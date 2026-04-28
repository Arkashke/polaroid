package polaroid.client.modules.impl.player;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;

@ModuleSystem(name = "NoEntityTrace", type = Category.Combat, server = ServerCategory.NO, description = "Убирает хитбокс игроков")
public class NoEntityTrace extends Module {
}


