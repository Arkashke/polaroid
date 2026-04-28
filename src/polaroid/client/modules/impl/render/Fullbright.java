package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import net.minecraft.potion.Effects;

@ModuleSystem(name = "Fullbright", type = Category.Render, server = ServerCategory.NO, description = "Делает все видимым в темноте")
public class Fullbright extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Гамма", "Гамма", "Эффект");
    
    private float previousGamma = 1.0F;

    public Fullbright() {
        addSettings(mode);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (mode.is("Эффект")) {
            if (!mc.player.isPotionActive(Effects.NIGHT_VISION)) {
                mc.player.removePotionEffect(Effects.NIGHT_VISION);
            }
        } else if (mode.is("Гамма")) {
            mc.gameSettings.gamma = 1000.0F;
        }
    }

    @Override
    public boolean onEnable() {
        super.onEnable();
        
        if (mc.gameSettings != null) {
            previousGamma = (float) mc.gameSettings.gamma;
            
            if (mode.is("Гамма")) {
                mc.gameSettings.gamma = 1000.0F;
            }
        }
        
        return false;
    }

    @Override
    public boolean onDisable() {
        super.onDisable();
        
        if (mc.gameSettings != null) {
            mc.gameSettings.gamma = previousGamma;
        }
        
        if (mc.player != null) {
            mc.player.removePotionEffect(Effects.NIGHT_VISION);
        }
        
        return false;
    }
}


