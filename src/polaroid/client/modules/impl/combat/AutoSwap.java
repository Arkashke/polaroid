package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventKey;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.movement.InventoryMove;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.utils.math.StopWatch;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;

@ModuleSystem(name = "AutoSwap", type = Category.Combat, server = ServerCategory.NO, description = "Автоматически меняет предметы")
public class AutoSwap extends Module {
    
    private final BindSetting keyToSwap = new BindSetting("Кнопка", -1);
    private final ModeSetting itemType = new ModeSetting("Предмет", "Тотем", "Щит", "Геплы", "Тотем", "Голова");
    private final ModeSetting swapType = new ModeSetting("Свапать на", "Голова", "Щит", "Геплы", "Тотем", "Голова");
    private final BooleanSetting spookyTime = new BooleanSetting("SpookyTime", false);
    
    private final StopWatch timer = new StopWatch();
    boolean swapped = true;
    boolean restart = true;

    public AutoSwap() {
        addSettings(keyToSwap, itemType, swapType, spookyTime);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (mc.player == null || mc.currentScreen != null) return;
        
        if (e.getKey() == keyToSwap.get()) {
            if (!timer.isReached(150)) return;
            
            if (restart) {
                String mode = itemType.get();
                if (mode.equals("Голова")) {
                    swapBall();
                } else {
                    swap(getItemByType(mode));
                }
                swapped = true;
                restart = false;
            } else {
                String mode = swapType.get();
                if (mode.equals("Голова")) {
                    swapBall();
                } else {
                    swap(getItemByType(mode));
                }
                swapped = true;
                restart = true;
            }
            
            timer.reset();
        }
    }

    public void swap(Item item) {
        if (!swapped) return;
        
        int slot = polaroid.client.utils.player.InventoryUtil.getItemSlot(item);
        if (slot == -1) {
            print("Предмет не найден");
            return;
        }
        
        if (spookyTime.get()) {
            // Отправляем пакет свапа с задержкой через GuiMove
            new Thread(() -> {
                try {
                    InventoryMove.resetTimer();
                    Thread.sleep(100);
                    mc.player.connection.sendPacketWithoutEvent(new net.minecraft.network.play.client.CClickWindowPacket(
                        0, slot, 40, ClickType.SWAP, ItemStack.EMPTY,
                        mc.player.container.getNextTransactionID(mc.player.inventory)
                    ));
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
        } else {
            mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
        }
        swapped = false;
    }

    public void swapBall() {
        for (int i = 0; i < 45; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            
            if (swapped && stack.getItem() instanceof SkullItem) {
                int slot = i < 9 ? i + 36 : i;
                
                if (spookyTime.get()) {
                    int finalSlot = slot;
                    new Thread(() -> {
                        try {
                            InventoryMove.resetTimer();
                            Thread.sleep(100);
                            mc.player.connection.sendPacketWithoutEvent(new net.minecraft.network.play.client.CClickWindowPacket(
                                0, finalSlot, 40, ClickType.SWAP, ItemStack.EMPTY,
                                mc.player.container.getNextTransactionID(mc.player.inventory)
                            ));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }).start();
                } else {
                    mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
                }
                swapped = false;
                break;
            } else if (!swapped && mc.player.getHeldItemOffhand().getItem() instanceof SkullItem) {
                int slot = polaroid.client.utils.player.InventoryUtil.getItemSlot(mc.player.getHeldItemOffhand().getItem());
                if (slot != -1) {
                    if (spookyTime.get()) {
                        int finalSlot = slot;
                        new Thread(() -> {
                            try {
                                InventoryMove.resetTimer();
                                Thread.sleep(100);
                                mc.player.connection.sendPacketWithoutEvent(new net.minecraft.network.play.client.CClickWindowPacket(
                                    0, finalSlot, 40, ClickType.SWAP, ItemStack.EMPTY,
                                    mc.player.container.getNextTransactionID(mc.player.inventory)
                                ));
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }).start();
                    } else {
                        mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
                    }
                    swapped = true;
                    break;
                }
            }
        }
    }

    private Item getItemByType(String type) {
        return switch (type) {
            case "Щит" -> Items.SHIELD;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Голова" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }
    
    @Override
    public boolean onDisable() {
        swapped = true;
        restart = true;
        return super.onDisable();
    }
}


