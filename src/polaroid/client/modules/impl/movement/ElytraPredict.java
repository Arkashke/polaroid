package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Items;

@ModuleSystem(name = "ElytraPredict", type = Category.Movement, server = ServerCategory.NO, description = "Предсказывает движение с элитрами")
public class ElytraPredict extends Module {

    private final SliderSetting speed = new SliderSetting("Скорость", 1.5f, 0.5f, 3.0f, 0.1f);
    private final BooleanSetting autoGlide = new BooleanSetting("Авто планирование", true);

    public ElytraPredict() {
        addSettings(speed, autoGlide);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() != Items.ELYTRA) {
            return;
        }

        if (mc.player.isElytraFlying()) {
            float speedValue = speed.get();
            
            if (mc.player.moveForward > 0) {
                mc.player.setMotion(
                    mc.player.getMotion().x * speedValue,
                    mc.player.getMotion().y,
                    mc.player.getMotion().z * speedValue
                );
            }

            if (autoGlide.get() && mc.player.getMotion().y < 0) {
                mc.player.setMotion(
                    mc.player.getMotion().x,
                    mc.player.getMotion().y * 0.6,
                    mc.player.getMotion().z
                );
            }
        }
    }
}


