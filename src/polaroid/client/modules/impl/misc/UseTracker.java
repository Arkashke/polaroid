package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;

@ModuleSystem(name = "UseTracker", type = Category.Misc, server = ServerCategory.NO, description = "Отслеживает использование предметов")
public class UseTracker extends Module {

    private final BooleanSetting showInChat = new BooleanSetting("Показывать в чате", true);
    private final BooleanSetting trackFood = new BooleanSetting("Отслеживать еду", true);
    private final BooleanSetting trackPotions = new BooleanSetting("Отслеживать зелья", true);
    private final BooleanSetting trackPearls = new BooleanSetting("Отслеживать жемчуг", true);

    private final Map<String, Integer> usageStats = new HashMap<>();
    private ItemStack lastUsedItem = ItemStack.EMPTY;

    public UseTracker() {
        addSettings(showInChat, trackFood, trackPotions, trackPearls);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (mc.player.isHandActive()) {
            ItemStack activeItem = mc.player.getActiveItemStack();
            
            if (!activeItem.isEmpty() && !ItemStack.areItemStacksEqual(activeItem, lastUsedItem)) {
                trackItemUse(activeItem);
                lastUsedItem = activeItem.copy();
            }
        } else {
            lastUsedItem = ItemStack.EMPTY;
        }
    }

    private void trackItemUse(ItemStack item) {
        String itemName = item.getDisplayName().getString();
        boolean shouldTrack = false;

        if (trackFood.get() && item.isFood()) {
            shouldTrack = true;
        }
        if (trackPotions.get() && itemName.contains("Potion")) {
            shouldTrack = true;
        }
        if (trackPearls.get() && itemName.contains("Pearl")) {
            shouldTrack = true;
        }

        if (shouldTrack) {
            int count = usageStats.getOrDefault(itemName, 0) + 1;
            usageStats.put(itemName, count);

            if (showInChat.get()) {
                print(TextFormatting.GREEN + itemName + TextFormatting.RESET + " использован " + 
                      TextFormatting.YELLOW + count + TextFormatting.RESET + " раз");
            }
        }
    }

    public Map<String, Integer> getUsageStats() {
        return new HashMap<>(usageStats);
    }

    public void resetStats() {
        usageStats.clear();
        print("Статистика использования сброшена");
    }

    @Override
    public boolean onDisable() {
        lastUsedItem = ItemStack.EMPTY;
        return super.onDisable();
    }
}


