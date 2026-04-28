package polaroid.client.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventKey;
import polaroid.client.events.EventPacket;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.utils.player.InventoryUtil;
import net.minecraft.item.Items;

@ModuleSystem(name = "ClickPearl", type = Category.Player, server = ServerCategory.NO, description = "Бросает эндер-жемчуг по кнопке")
public class ClickPearl extends Module {

    private final ModeSetting pearlMode = new ModeSetting("Режим", "Пакетный", "Пакетный", "Легитный");
    private final BindSetting pearlKey = new BindSetting("Кнопка", -98);
    private final polaroid.client.modules.settings.impl.BooleanSetting returnPearl = new polaroid.client.modules.settings.impl.BooleanSetting("Возврат в инвентарь", false);
    
    private final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();
    private long delay;

    public ClickPearl() {
        addSettings(pearlMode, pearlKey, returnPearl);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == pearlKey.get() && !mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
            if (pearlMode.is("Пакетный")) {
                InventoryUtil.inventorySwapClick(Items.ENDER_PEARL, true, returnPearl.get());
            }
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }
}


