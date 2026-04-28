package polaroid.client.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventKey;
import polaroid.client.events.EventPacket;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.player.InventoryUtil;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@ModuleSystem(name = "FunTimeAssist", type = Category.Misc, description = "Помощник для сервера FunTime и его копий", server = ServerCategory.NO)
public class GriefHelper extends Module {

    private final ModeListSetting mode = new ModeListSetting("Тип",
            new BooleanSetting("Использование по бинду", true));

    private final BindSetting disorientationKey = new BindSetting("Кнопка дезориентации", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting shulkerKey = new BindSetting("Кнопка шалкера", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting trapKey = new BindSetting("Кнопка трапки", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting flameKey = new BindSetting("Кнопка огненного смерча", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting blatantKey = new BindSetting("Кнопка явной пыли", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting bowKey = new BindSetting("Кнопка арбалета", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting plastKey = new BindSetting("Кнопка пласта", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting snowKey = new BindSetting("Кнопка снежка", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting bojauraKey = new BindSetting("Кнопка божьей ауры", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting charkaKey = new BindSetting("Кнопка чарки", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());
    private final BindSetting healKey = new BindSetting("Кнопка исцеления", -1)
            .setVisible(() -> mode.getValueByName("Использование по бинду").get());

    final StopWatch stopWatch = new StopWatch();
    InventoryUtil.Hand handUtil = new InventoryUtil.Hand();
    long delay;
    boolean disorientationThrow, trapThrow, flameThrow, blatantThrow, bowThrow, plastThrow, shulkerThrow, snowThrow, bojauraThrow, charkaThrow, healThrow;
    
    // Для отслеживания зажатия кнопок чарки и исцеления
    boolean charkaPressed = false;
    boolean healPressed = false;
    int originalSlotCharka = -1;
    int originalSlotHeal = -1;
    int charkaSlot = -1;
    int healSlot = -1;

    public GriefHelper() {
        addSettings(mode, disorientationKey, trapKey, flameKey, blatantKey, bowKey, plastKey, shulkerKey, snowKey, bojauraKey, charkaKey, healKey);
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == disorientationKey.get()) {
            disorientationThrow = true;
        }
        if (e.getKey() == shulkerKey.get()) {
            shulkerThrow = true;
        }
        if (e.getKey() == trapKey.get()) {
            trapThrow = true;
        }
        if (e.getKey() == flameKey.get()) {
            flameThrow = true;
        }
        if (e.getKey() == blatantKey.get()) {
            blatantThrow = true;
        }
        if (e.getKey() == bowKey.get()) {
            bowThrow = true;
        }
        if (e.getKey() == plastKey.get()) {
            plastThrow = true;
        }
        if (e.getKey() == snowKey.get()) {
            snowThrow = true;
        }
        if (e.getKey() == bojauraKey.get()) {
            bojauraThrow = true;
        }
        if (e.getKey() == charkaKey.get()) {
            charkaThrow = true;
        }
        if (e.getKey() == healKey.get()) {
            healThrow = true;
        }
    }
    
    private boolean isKeyPressed(int key) {
        if (key == -1 || key == 0) return false; // 0 = GLFW_KEY_UNKNOWN, invalid for glfwGetKey
        return org.lwjgl.glfw.GLFW.glfwGetKey(mc.getMainWindow().getHandle(), key) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        // Обработка чарки с зажатием
        if (isKeyPressed(charkaKey.get())) {
            if (!charkaPressed) {
                // Первое нажатие - переключаем слот
                int hbSlot = getItemForName("зачарованное золотое яблоко", true);
                if (hbSlot == -1) {
                    hbSlot = getItem(Items.ENCHANTED_GOLDEN_APPLE, true);
                }
                if (hbSlot != -1) {
                    originalSlotCharka = mc.player.inventory.currentItem;
                    charkaSlot = hbSlot;
                    mc.player.inventory.currentItem = hbSlot;
                    charkaPressed = true;
                    // Начинаем использовать предмет
                    mc.gameSettings.keyBindUseItem.setPressed(true);
                } else {
                    print("Чарка не найдена в хотбаре!");
                }
            }
            // Пока кнопка зажата - предмет автоматически используется
        } else {
            if (charkaPressed) {
                // Кнопка отпущена - останавливаем использование и возвращаем слот
                mc.gameSettings.keyBindUseItem.setPressed(false);
                if (originalSlotCharka != -1) {
                    mc.player.inventory.currentItem = originalSlotCharka;
                    originalSlotCharka = -1;
                }
                charkaPressed = false;
                charkaSlot = -1;
            }
        }
        
        // Обработка исцеления с зажатием
        if (isKeyPressed(healKey.get())) {
            if (!healPressed) {
                // Первое нажатие - переключаем слот
                int hbSlot = getItemForName("исцеление", true);
                if (hbSlot == -1) {
                    hbSlot = getItem(Items.POTION, true);
                }
                if (hbSlot != -1) {
                    originalSlotHeal = mc.player.inventory.currentItem;
                    healSlot = hbSlot;
                    mc.player.inventory.currentItem = hbSlot;
                    healPressed = true;
                    // Начинаем использовать предмет
                    mc.gameSettings.keyBindUseItem.setPressed(true);
                } else {
                    print("Исцеление не найдено в хотбаре!");
                }
            }
            // Пока кнопка зажата - предмет автоматически используется
        } else {
            if (healPressed) {
                // Кнопка отпущена - останавливаем использование и возвращаем слот
                mc.gameSettings.keyBindUseItem.setPressed(false);
                if (originalSlotHeal != -1) {
                    mc.player.inventory.currentItem = originalSlotHeal;
                    originalSlotHeal = -1;
                }
                healPressed = false;
                healSlot = -1;
            }
        }
        
        if (disorientationThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("дезориентация", true);
            int invSlot = getItemForName("дезориентация", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Дезориентация не найдена!");
                disorientationThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)) {
                print("Заюзал дезориентацию!");
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            disorientationThrow = false;
        }
        if (shulkerThrow) {
            // САЛАТ СПАСАЙ
            int hbSlot = getItemForName("ящик", true); // мега поиск ящика
            int invSlot = getItemForName("ящик", false); // мега поиск ящика


            if (invSlot == -1 && hbSlot == -1) {
                print("Шалкер не найден");
                trapThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SHULKER_BOX)) {
                print("Заюзал шалкер!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            shulkerThrow = false;
        }

        if (trapThrow) {
            int hbSlot = getItemForName("трапка", true);
            int invSlot = getItemForName("трапка", false);


            if (invSlot == -1 && hbSlot == -1) {
                print("Трапка не найдена");
                trapThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHERITE_SCRAP)) {
                print("Заюзал трапку!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            trapThrow = false;
        }
        if (flameThrow) {
            int hbSlot = getItemForName("огненный", true);
            int invSlot = getItemForName("огненный", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Огненный смерч не найден");
                flameThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.FIRE_CHARGE)) {
                print("Заюзал огненный смерч!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            flameThrow = false;
        }
        if (bowThrow) {
            int hbSlot = getItemForName("арбалет", true);
            int invSlot = getItemForName("арбалет", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Арбалет не найден");
                bowThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.CROSSBOW)) {
                print("Заюзал арбалет!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            bowThrow = false;
        }
        if (plastThrow) {
            int hbSlot = getItemForName("пласт", true);
            int invSlot = getItemForName("пласт", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Пласт не найден");
                plastThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP)) {
                print("Заюзал пласт!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            plastThrow = false;
        }
        if (blatantThrow) {
            int hbSlot = getItemForName("явная", true);
            int invSlot = getItemForName("явная", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Явная пыль не найдена");
                blatantThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.TNT)) {
                print("Заюзал явную пыль!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            blatantThrow = false;
        }
        if (snowThrow) {
            int hbSlot = getItemForName("снежок замарозка", true);
            int invSlot = getItemForName("снежок замарозка", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Снежок не найден");
                snowThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SNOWBALL)) {
                print("Заюзал снежок!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            snowThrow = false;
        }
        if (bojauraThrow) {
            int hbSlot = getItemForName("божья аура", true);
            int invSlot = getItemForName("божья аура", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Божья аура не найдена");
                bojauraThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.PHANTOM_MEMBRANE)) {
                print("Заюзал божью ауру!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            bojauraThrow = false;
        }
        if (charkaThrow) {
            // Старая логика для одиночного нажатия (если нужна)
            charkaThrow = false;
        }
        if (healThrow) {
            // Старая логика для одиночного нажатия (если нужна)
            healThrow = false;
        }
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
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

    @Override
    public boolean onDisable() {
        disorientationThrow = false;
        trapThrow = false;
        flameThrow = false;
        blatantThrow = false;
        plastThrow = false;
        bowThrow = false;
        snowThrow = false;
        bojauraThrow = false;
        charkaThrow = false;
        healThrow = false;
        
        // Сброс состояния зажатия
        if (charkaPressed && originalSlotCharka != -1) {
            mc.gameSettings.keyBindUseItem.setPressed(false);
            mc.player.inventory.currentItem = originalSlotCharka;
        }
        if (healPressed && originalSlotHeal != -1) {
            mc.gameSettings.keyBindUseItem.setPressed(false);
            mc.player.inventory.currentItem = originalSlotHeal;
        }
        charkaPressed = false;
        healPressed = false;
        originalSlotCharka = -1;
        originalSlotHeal = -1;
        charkaSlot = -1;
        healSlot = -1;
        
        delay = 0;
        super.onDisable();
        return false;
    }

    private int getItem(Item input, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }
            if (itemStack.getItem() == input) {
                return i;
            }
        }
        return -1;
    }
    
    private int getItemForName(String name, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }

            String displayName = TextFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName().getString());
            if (displayName != null && displayName.toLowerCase().contains(name)) {
                return i;
            }
        }
        return -1;
    }
}

