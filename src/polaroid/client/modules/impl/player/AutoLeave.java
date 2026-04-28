package polaroid.client.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventKey;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.StopWatch;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleSystem(name = "AutoLeaveFT", type=  Category.Player, server = ServerCategory.NO, description = "Автоматически ливает на /darena")
public class AutoLeave extends Module {
    private final ModeSetting mode = new ModeSetting("Тип", "FunTime", "FunTime");
    private final SliderSetting stealDelay = new SliderSetting("Задержка", 1, 1, 50, 1);
    private final BindSetting bind = new BindSetting("Бинд", -1);
    private final ModeSetting filterLoot = new ModeSetting("Режим", "/darena [FT]","/darena [FT]");
    private final StopWatch timerUtil = new StopWatch();

    public AutoLeave() {
        addSettings(stealDelay, filterLoot, bind);
    }

    private boolean filterItem(Item item) {
        return filterLoot.is("/darena [FT]") && item == Items.PUFFERFISH;
    }

    @Subscribe
    public void onEventKey(EventKey e) {
        if (e.getKey() == bind.get()) {
            if (mc.player != null) {
                mc.player.sendChatMessage("/darena");
            }
        }
    }

    @Subscribe
    public void onEvent(final EventUpdate event) {
        if (mode.is("Стандарт")) {
            if (mc.player.openContainer instanceof ChestContainer) {
                ChestContainer container = (ChestContainer) mc.player.openContainer;
                IInventory inventory = container.getLowerChestInventory();
                List<Integer> validSlots = new ArrayList<>();

                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    if (inventory.getStackInSlot(i).getItem() != Item.getItemById(0)
                            && inventory.getStackInSlot(i).getCount() <= 64
                            && filterItem(inventory.getStackInSlot(i).getItem())) {
                        validSlots.add(i);
                    }
                }

                if (!validSlots.isEmpty() && timerUtil.isReached(Math.round(stealDelay.get()))) {
                    int randomIndex = new Random().nextInt(validSlots.size());
                    int slotToSteal = validSlots.get(randomIndex);
                    mc.playerController.windowClick(container.windowId, slotToSteal, 0, ClickType.QUICK_MOVE, mc.player);
                    timerUtil.reset();
                }
            }
        }
    }
}


