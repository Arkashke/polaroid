package polaroid.client.modules.impl.render;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;

@ModuleSystem(name = "NoHandShake", type = Category.Render, server = ServerCategory.NO, description = "Отключает тряску рук при быстром движении камеры")
public class NoHandShake extends Module {

    public NoHandShake() {
    }
}


