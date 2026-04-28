package polaroid.client.utils.player;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import polaroid.client.Polaroid;
import polaroid.client.events.EventPacket;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.utils.client.IMinecraft;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.server.SHeldItemChangePacket;

public class InventoryUtil implements IMinecraft {

    @Getter
    private static InventoryUtil instance = new InventoryUtil();


    public static int findEmptySlot(boolean inHotBar) {
        int start = inHotBar ? 0 : 9;
        int end = inHotBar ? 9 : 45;

        for (int i = start; i < end; ++i) {
            if (!mc.player.inventory.getStackInSlot(i).isEmpty()) {
                continue;
            }

            return i;
        }
        return -1;
    }

    public static void moveItem(int from, int to, boolean air) {

        if (from == to)
            return;
        pickupItem(from, 0);
        pickupItem(to, 0);
        if (air)
            pickupItem(from, 0);
    }


    public static void moveItemTime(int from, int to, boolean air, int time) {

        if (from == to)
            return;
        pickupItem(from, 0, time);
        pickupItem(to, 0, time);
        if (air)
            pickupItem(from, 0, time);
    }

    public static void moveItem(int from, int to) {
        if (from == to)
            return;
        pickupItem(from, 0);
        pickupItem(to, 0);
        pickupItem(from, 0);
    }

    public static void pickupItem(int slot, int button) {
        mc.playerController.windowClick(0, slot, button, ClickType.PICKUP, mc.player);
    }

    public static void pickupItem(int slot, int button, int time) {
        mc.playerController.windowClickFixed(0, slot, button, ClickType.PICKUP, mc.player, time);
    }

    public static void inventorySwapClick(Item item, boolean rotation) {
        inventorySwapClick(item, rotation, true);
    }

    public static void inventorySwapClick(Item item, boolean rotation, boolean returnToInventory) {
        if (InventoryHelper.getItemIndex(item) != -1) {
            int i;
            if (doesHotbarHaveItem(item)) {
                for (i = 0; i < 9; ++i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                        if (i != mc.player.inventory.currentItem) {
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
                        }

                        if (rotation) {
                            Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
                            if (aura.getTarget() != null) {
                                mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                            }
                        }

                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                        if (i != mc.player.inventory.currentItem) {
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                        }
                        break;
                    }
                }
            }

            // Если предмет не в hotbar - свапаем из инвентаря через пакеты с задержкой
            if (!doesHotbarHaveItem(item)) {
                for (i = 0; i < 36; ++i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                        int targetSlot = mc.player.inventory.currentItem % 8 + 1;
                        int finalSlot = i;
                        
                        // Используем поток с задержкой как в AutoSwap
                        new Thread(() -> {
                            try {
                                polaroid.client.modules.impl.movement.InventoryMove.resetTimer();
                                Thread.sleep(100);
                                
                                // Свапаем через пакет
                                mc.player.connection.sendPacketWithoutEvent(new net.minecraft.network.play.client.CClickWindowPacket(
                                    0, finalSlot < 9 ? finalSlot + 36 : finalSlot, targetSlot, ClickType.SWAP,
                                    net.minecraft.item.ItemStack.EMPTY, mc.player.container.getNextTransactionID(mc.player.inventory)
                                ));
                                
                                mc.player.connection.sendPacket(new CHeldItemChangePacket(targetSlot));
                                
                                if (rotation) {
                                    Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
                                    if (aura.getTarget() != null) {
                                        mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                                    }
                                }

                                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                                
                                // Свапаем обратно только если включен возврат
                                if (returnToInventory) {
                                    mc.player.connection.sendPacketWithoutEvent(new net.minecraft.network.play.client.CClickWindowPacket(
                                        0, finalSlot < 9 ? finalSlot + 36 : finalSlot, targetSlot, ClickType.SWAP,
                                        net.minecraft.item.ItemStack.EMPTY, mc.player.container.getNextTransactionID(mc.player.inventory)
                                    ));
                                }
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }).start();
                        break;
                    }
                }
            }
        }
    }

    public static boolean doesHotbarHaveItem(Item item) {
        for (int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public int getAxeInInventory(boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;

        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    public int findBestSlotInHotBar() {
        int emptySlot = findEmptySlot();
        if (emptySlot != -1) {
            return emptySlot;
        } else {
            return findNonSwordSlot();
        }
    }

    private int findEmptySlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty() && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    private int findNonSwordSlot() {
        for (int i = 0; i < 9; i++) {
            if (!(mc.player.inventory.getStackInSlot(i).getItem() instanceof SwordItem) && !(mc.player.inventory.getStackInSlot(i).getItem() instanceof ElytraItem) && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    public int getSlotInInventory(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public int getSlotInInventoryOrHotbar(Item item, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static int getSlotInInventoryOrHotbar() {
        int firstSlot = 0;
        int lastSlot = 9;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {

            if (Block.getBlockFromItem(mc.player.inventory.getStackInSlot(i).getItem()) instanceof SlabBlock) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }
    
    // Метод из avalora - ищет предмет в инвентаре и возвращает правильный слот
    public static int getItemSlot(Item input) {
        // Проверяем броню
        for (net.minecraft.item.ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getItem() == input) {
                return -2;
            }
        }

        int slot = -1;
        for (int i = 0; i < 36; ++i) {
            net.minecraft.item.ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public static class Hand {
        public static boolean isEnabled;
        private boolean isChangingItem;
        private int originalSlot = -1;

        public void onEventPacket(EventPacket eventPacket) {
            if (!eventPacket.isReceive()) {
                return;
            }
            if (eventPacket.getPacket() instanceof SHeldItemChangePacket) {
                this.isChangingItem = true;
            }
        }

        public void handleItemChange(boolean resetItem) {
            if (this.isChangingItem && this.originalSlot != -1) {
                isEnabled = true;
                mc.player.inventory.currentItem = this.originalSlot;
                if (resetItem) {
                    this.isChangingItem = false;
                    this.originalSlot = -1;
                    isEnabled = false;
                }
            }
        }

        public void setOriginalSlot(int slot) {
            this.originalSlot = slot;
        }
    }

}


