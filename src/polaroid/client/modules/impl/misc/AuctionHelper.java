package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.Setting;
import polaroid.client.modules.settings.impl.BooleanSetting;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import polaroid.client.modules.settings.impl.ColorSetting;
import polaroid.client.utils.render.ColorUtils;

@Getter
@ModuleSystem(name = "AuctionHelper", type = Category.Misc, server = ServerCategory.NO, description = "Показывает самый дешёвый предмет на аукционе")
public class AuctionHelper extends Module {
    public BooleanSetting three = new BooleanSetting("Показ. 3 предмета", true);
    public ColorSetting color1 = new ColorSetting("Цвет первого по цене", ColorUtils.rgba(64, 255, 64, 100));
    public ColorSetting color2 = new ColorSetting("Цвет второго по цене", ColorUtils.rgba(255, 255, 64, 100));
    public ColorSetting color3 = new ColorSetting("Цвет третьего по цене", ColorUtils.rgba(255, 64, 64, 100));
    
    float x = 0.0F;
    float y = 0.0F;
    float x2 = 0.0F;
    float y2 = 0.0F;
    float x3 = 0.0F;
    float y3 = 0.0F;

    public AuctionHelper() {
        this.addSettings(new Setting[]{this.three, this.color1, this.color2, this.color3});
    }

    @Subscribe
    public void onUpdate(EventUpdate update) {
        Screen var3 = mc.currentScreen;
        if (var3 instanceof ChestScreen e) {
            if (!e.getTitle().getString().contains("Аукцион") && !e.getTitle().getString().contains("Поиск:")) {
                this.setX(0.0F);
                this.setX2(0.0F);
                this.setX3(0.0F);
            } else {
                Container container = e.getContainer();
                Slot slot1 = null;
                Slot slot2 = null;
                Slot slot3 = null;
                
                double fsPrice = Double.MAX_VALUE;
                double medPrice = Double.MAX_VALUE;
                double thPrice = Double.MAX_VALUE;

                for (Slot slot : container.inventorySlots) {
                    if (slot.slotNumber > 44) continue;

                    ItemStack stack = slot.getStack();
                    int totalPrice = extractPriceFromStack(stack);
                    int count = stack.getCount();

                    if (totalPrice == -1 || count == 0) continue;

                    double pricePerItem = (double) totalPrice / count;

                    if (pricePerItem < fsPrice) {
                        thPrice = medPrice;
                        slot3 = slot2;
                        medPrice = fsPrice;
                        slot2 = slot1;
                        fsPrice = pricePerItem;
                        slot1 = slot;
                    } else if (three.get() && pricePerItem < medPrice) {
                        thPrice = medPrice;
                        slot3 = slot2;
                        medPrice = pricePerItem;
                        slot2 = slot;
                    } else if (three.get() && pricePerItem < thPrice) {
                        thPrice = pricePerItem;
                        slot3 = slot;
                    }
                }

                if (slot1 != null) {
                    this.setX((float)slot1.xPos);
                    this.setY((float)slot1.yPos);
                } else {
                    this.setX(0.0F);
                    this.setY(0.0F);
                }
                
                if (slot2 != null) {
                    this.setX2((float)slot2.xPos);
                    this.setY2((float)slot2.yPos);
                } else {
                    this.setX2(0.0F);
                    this.setY2(0.0F);
                }
                
                if (slot3 != null) {
                    this.setX3((float)slot3.xPos);
                    this.setY3((float)slot3.yPos);
                } else {
                    this.setX3(0.0F);
                    this.setY3(0.0F);
                }
            }
        } else {
            this.setX(0.0F);
            this.setX2(0.0F);
            this.setX3(0.0F);
        }
    }

    protected int extractPriceFromStack(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("display", 10)) {
            CompoundNBT display = tag.getCompound("display");
            if (display.contains("Lore", 9)) {
                ListNBT lore = display.getList("Lore", 8);

                for(int j = 0; j < lore.size(); ++j) {
                    JsonObject object = new JsonParser().parse(lore.getString(j)).getAsJsonObject();
                    if (object.has("extra")) {
                        JsonArray array = object.getAsJsonArray("extra");
                        if (array.size() > 2) {
                            JsonObject title = array.get(1).getAsJsonObject();
                            if (title.get("text").getAsString().trim().toLowerCase().contains("ценa")) {
                                String line = array.get(2).getAsJsonObject().get("text").getAsString().trim()
                                        .substring(1).replaceAll(" ", "").replaceAll(",", "");
                                try {
                                    return Integer.parseInt(line);
                                } catch (NumberFormatException e) {
                                    return -1;
                                }
                            }
                        }
                    }
                }
            }
        }

        return -1;
    }

    public BooleanSetting getThree() {
        return this.three;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getX2() {
        return this.x2;
    }

    public float getY2() {
        return this.y2;
    }

    public float getX3() {
        return this.x3;
    }

    public float getY3() {
        return this.y3;
    }

    public void setThree(BooleanSetting three) {
        this.three = three;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public void setX3(float x3) {
        this.x3 = x3;
    }

    public void setY3(float y3) {
        this.y3 = y3;
    }
}

