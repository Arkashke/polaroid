package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventKey;
import polaroid.client.events.EventPacket;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.movement.InventoryMove;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleSystem(name = "ElytraHelper", type = Category.Misc, server = ServerCategory.NO, description = "Позволяет свапать и использовать феерверк на определённую кнопку")
public class ElytraHelper extends Module {

    final BindSetting swapChestKey = new BindSetting("Кнопка свапа", -1);
    final BindSetting fireWorkKey = new BindSetting("Кнопка феерверков", -1);
    final BooleanSetting autoFly = new BooleanSetting("Авто взлёт", true);
    final BooleanSetting spookyTime = new BooleanSetting("SpookyTime", false);
    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();

    public ElytraHelper() {
        addSettings(swapChestKey, fireWorkKey, autoFly, spookyTime);
    }

    ItemStack currentStack = ItemStack.EMPTY;
    public static StopWatch stopWatch = new StopWatch();
    long delay;
    boolean fireworkUsed;

    @Subscribe
    private void onEventKey(EventKey e) {
        if (e.getKey() == swapChestKey.get() && stopWatch.isReached(100L)) {
            changeChestPlate(currentStack);
            stopWatch.reset();
        }

        if (e.getKey() == fireWorkKey.get() && stopWatch.isReached(200L)) {
            fireworkUsed = true;
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (autoFly.get() && currentStack.getItem() == Items.ELYTRA) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else if (ElytraItem.isUsable(currentStack) && !mc.player.isElytraFlying()) {
                mc.player.startFallFlying();
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }
        
        if (fireworkUsed) {
            int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true);
            int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, false);
            
            if (invSlot == -1 && hbSlot == -1) {
                print("Феерверки не найдены!");
                fireworkUsed = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.FIREWORK_ROCKET)) {
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            fireworkUsed = false;
        }
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handUtil.onEventPacket(e);
    }

    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }
        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }
    
    private void changeChestPlate(ItemStack stack) {
        if (mc.currentScreen != null) {
            return;
        }
        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                swapArmor(elytraSlot);
                print(TextFormatting.RED + "Свапнул на элитру!");
                return;
            } else {
                print("Элитра не найдена!");
            }
        }
        int armorSlot = getChestPlateSlot();
        if (armorSlot >= 0) {
            swapArmor(armorSlot);
            print(TextFormatting.RED + "Свапнул на нагрудник!");
        } else {
            print("Нагрудник не найден!");
        }
    }
    
    // Свап брони с остановкой движения на 400мс
    private void swapArmor(int slot) {
        if (spookyTime.get()) {
            // SpookyTime режим - с остановкой движения
            new Thread(() -> {
                try {
                    // Останавливаем движение на 400мс
                    InventoryMove.resetTimer();
                    Thread.sleep(100);
                    
                    // Свапаем броню через три клика с задержками
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                    Thread.sleep(50);
                    mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
                    Thread.sleep(50);
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        } else {
            // Обычный режим - свап с минимальными задержками
            new Thread(() -> {
                try {
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                    Thread.sleep(25);
                    mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
                    Thread.sleep(25);
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        }
    }
    
    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    if (i < 9) {
                        i += 36;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean onDisable() {
        stopWatch.reset();
        super.onDisable();
        return false;
    }

    private int getItemSlot(Item input) {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }
}


