package polaroid.client.modules.impl.combat;

import polaroid.client.events.EventPacket;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.movement.InventoryMove;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.player.InventoryUtil;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@ModuleSystem(
        name = "AutoTotem",
        type = Category.Combat,
        server = ServerCategory.NO,
        description = "Берёт тотемы при низком значение здоровья"
)
public class AutoTotem extends Module {
    private final SliderSetting health = new SliderSetting("Здоровье", 5.0f, 1.0f, 20.0f, 0.5f);
    private final SliderSetting healthelytra = new SliderSetting("Здоровье на элитрах", 9f, 1.0f, 20.0f, 0.5f);
    private final SliderSetting healthbronya = new SliderSetting("Без полной брони", 8f, 1.0f, 20.0F, 0.5F);
    private final ModeListSetting mode = new ModeListSetting("Проверки на",
            new BooleanSetting("Золотые сердца", true),
            new BooleanSetting("Кристаллы", true),
            new BooleanSetting("Падение", true),
            new BooleanSetting("Кристалл в руке", true)
    );
    
    private final BooleanSetting swapBack = new BooleanSetting("Возвращать предмет", true);
    private final BooleanSetting noBallSwitch = new BooleanSetting("Не сменять шар", false);
    private final BooleanSetting saveEnchanted = new BooleanSetting("Сохранять зачарованный", true);
    private final BooleanSetting noWhileEating = new BooleanSetting("Не при еде", false);
    private final BooleanSetting spookyTime = new BooleanSetting("SpookyTime", false);

    private int nonEnchantedTotems;
    private int oldItem = -1;
    private int totemCount = 0;
    private boolean totemIsUsed;
    ItemStack currentStack = ItemStack.EMPTY;

    public AutoTotem() {
        addSettings(mode, health, healthelytra, healthbronya, swapBack, noBallSwitch, saveEnchanted, noWhileEating, spookyTime);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        totemCount = countTotems(true);
        nonEnchantedTotems = (int) IntStream.range(0, 36)
                .mapToObj(i -> mc.player.inventory.getStackInSlot(i))
                .filter(s -> s.getItem() == Items.TOTEM_OF_UNDYING && !s.isEnchanted())
                .count();

        int slot = getSlotInInventory();
        boolean handNotNull = !(mc.player.getHeldItemOffhand().getItem() instanceof AirItem);

        if (shouldToSwapTotem()) {
            if (slot != -1 && !isTotemInHands()) {
                if (spookyTime.get()) {
                    // Блокируем движение СРАЗУ, потом отправляем пакет с задержкой
                    InventoryMove.resetTimer();
                    int finalSlot = slot;
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            mc.player.connection.sendPacketWithoutEvent(new CClickWindowPacket(
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
                if (handNotNull && oldItem == -1) {
                    oldItem = slot;
                }
            }
        } else if (oldItem != -1 && swapBack.get()) {
            if (spookyTime.get()) {
                // Блокируем движение СРАЗУ, потом отправляем пакет с задержкой
                InventoryMove.resetTimer();
                int finalOldItem = oldItem;
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        mc.player.connection.sendPacketWithoutEvent(new CClickWindowPacket(
                            0, finalOldItem, 40, ClickType.SWAP, ItemStack.EMPTY,
                            mc.player.container.getNextTransactionID(mc.player.inventory)
                        ));
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
            } else {
                mc.playerController.windowClick(0, oldItem, 40, ClickType.SWAP, mc.player);
            }
            oldItem = -1;
        }
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.isReceive()) {
            if (event.getPacket() instanceof SEntityStatusPacket statusPacket) {
                if (statusPacket.getOpCode() == 35 && statusPacket.getEntity(mc.world) == mc.player) {
                    totemIsUsed = true;
                }
            }
        }
    }

    private int countTotems(boolean includeEnchanted) {
        return (int) IntStream.range(0, mc.player.inventory.getSizeInventory())
                .mapToObj(i -> mc.player.inventory.getStackInSlot(i))
                .filter(s -> s.getItem() == Items.TOTEM_OF_UNDYING && (includeEnchanted || !s.isEnchanted()))
                .count();
    }

    private boolean isTotemInHands() {
        for (Hand hand : Hand.values()) {
            ItemStack heldItem = mc.player.getHeldItem(hand);
            if (heldItem.getItem() == Items.TOTEM_OF_UNDYING && !isSaveEnchanted(heldItem)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSaveEnchanted(ItemStack itemStack) {
        return saveEnchanted.get() && itemStack.isEnchanted() && nonEnchantedTotems > 0;
    }

    private boolean shouldToSwapTotem() {
        // Твоя функция - не брать при еде
        if (noWhileEating.get() && mc.player.isHandActive()) {
            ItemStack activeItem = mc.player.getActiveItemStack();
            if (activeItem.getItem().isFood() || activeItem.getItem() instanceof PotionItem) {
                return false;
            }
        }

        this.currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        final float absorptionAmount = mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f;
        float currentHealth = mc.player.getHealth();
        if (this.mode.getValueByName("Золотые сердца").get()) {
            currentHealth += absorptionAmount;
        }

        boolean hasFullArmor = true;
        for (int i = 0; i < mc.player.inventory.armorInventory.size(); i++) {
            ItemStack armor = mc.player.inventory.armorInventory.get(i);
            if (armor.isEmpty()) {
                if (i == 3 && hasJackHeadInInventory()) {
                    continue;
                }
                hasFullArmor = false;
                break;
            }
        }

        float healthThreshold = hasFullArmor ? this.health.get() : this.healthbronya.get();

        return (!this.isOffhandItemBall() && this.isInDangerousSituation()) ||
                currentHealth <= (currentStack.getItem() == Items.ELYTRA ? healthelytra.get() : healthThreshold) ||
                checkFall();
    }

    private boolean isInDangerousSituation() {
        return checkCrystal() || checkPlayerWithCrystalNearObsidian();
    }

    private boolean checkFall() {
        if (!this.mode.getValueByName("Падение").get()) {
            return false;
        }
        if (mc.player.isInWater() || mc.player.isElytraFlying()) {
            return false;
        }
        float fallDistance = mc.player.fallDistance;
        float fallDamage = calculateFallDamage(fallDistance);

        float currentHealth = mc.player.getHealth();

        return fallDamage >= currentHealth / 1.92f;
    }

    private float calculateFallDamage(float fallDistance) {
        if (fallDistance <= 3.0f) return 0;

        float fallDamage = (fallDistance - 3.0f) / 2;

        float armorReduction = 0;
        for (ItemStack armor : mc.player.inventory.armorInventory) {
            if (armor.getItem() instanceof ArmorItem) {
                armorReduction += ((ArmorItem) armor.getItem()).getDamageReduceAmount();
            }
        }

        ItemStack boots = mc.player.inventory.armorInventory.get(0);
        if (boots.getItem() instanceof ArmorItem) {
            int featherFallingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FEATHER_FALLING, boots);
            if (featherFallingLevel > 0) {
                float reductionFactor = 1.0f - (Math.min(featherFallingLevel, 4) * 0.171f);
                fallDamage *= reductionFactor;
            }
        }

        if (hasProtectionAura()) {
            fallDamage *= 0.2f;
        }

        float absorption = mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f;
        fallDamage = Math.max(0, fallDamage - absorption);

        return Math.min(fallDamage, mc.player.getMaxHealth());
    }

    private boolean hasProtectionAura() {
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.hasDisplayName() && "Аура Защиты От Падения".equals(stack.getDisplayName().getString())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCrystal() {
        if (!mode.getValueByName("Кристаллы").get())
            return false;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderCrystalEntity && mc.player.getDistance(entity) <= 6.0f) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPlayerWithCrystalNearObsidian() {
        if (!mode.getValueByName("Кристалл в руке").get())
            return false;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof PlayerEntity && entity != mc.player && mc.player.getDistance(entity) <= 5.0f) {
                PlayerEntity otherPlayer = (PlayerEntity) entity;
                ItemStack mainHand = otherPlayer.getHeldItemMainhand();
                ItemStack offHand = otherPlayer.getHeldItemOffhand();
                if (mainHand.getItem() == Items.END_CRYSTAL || offHand.getItem() == Items.END_CRYSTAL) {
                    BlockPos obsidianPos = getBlock(5.0f, Blocks.OBSIDIAN);
                    if (obsidianPos != null && getDistanceOfEntityToBlock(otherPlayer, obsidianPos) <= 5.0f) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isOffhandItemBall() {
        return noBallSwitch.get() && mc.player.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD;
    }

    private BlockPos getBlock(float distance, Block block) {
        return getSphere(getPlayerPosLocal(), distance, 6, false, true, 0).stream()
                .filter(position -> mc.world.getBlockState(position).getBlock() == block)
                .min(Comparator.comparing(blockPos -> getDistanceOfEntityToBlock(mc.player, blockPos)))
                .orElse(null);
    }

    private List<BlockPos> getSphere(BlockPos center, float radius, int height, boolean hollow, boolean fromBottom, int yOffset) {
        List<BlockPos> positions = new ArrayList<>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();
        for (int x = centerX - (int) radius; x <= centerX + radius; ++x) {
            for (int z = centerZ - (int) radius; z <= centerZ + radius; ++z) {
                int yStart = fromBottom ? (centerY - (int) radius) : centerY;
                for (int yEnd = fromBottom ? (centerY + (int) radius) : (centerY + height), y = yStart; y < yEnd; ++y) {
                    if (isPositionWithinSphere(centerX, centerY, centerZ, x, y, z, radius, hollow)) {
                        positions.add(new BlockPos(x, y + yOffset, z));
                    }
                }
            }
        }
        return positions;
    }

    private BlockPos getPlayerPosLocal() {
        if (mc.player == null) {
            return BlockPos.ZERO;
        }
        return new BlockPos(Math.floor(mc.player.getPosX()), Math.floor(mc.player.getPosY()), Math.floor(mc.player.getPosZ()));
    }

    private double getDistanceOfEntityToBlock(Entity entity, BlockPos blockPos) {
        return getDistance(entity.getPosX(), entity.getPosY(), entity.getPosZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    private double getDistance(double n, double n2, double n3, double n4, double n5, double n6) {
        double n7 = n - n4;
        double n8 = n2 - n5;
        double n9 = n3 - n6;
        return MathHelper.sqrt(n7 * n7 + n8 * n8 + n9 * n9);
    }

    private static boolean isPositionWithinSphere(int centerX, int centerY, int centerZ, int x, int y, int z, float radius, boolean hollow) {
        double distanceSq = Math.pow(centerX - x, 2.0) + Math.pow(centerZ - z, 2.0) + Math.pow(centerY - y, 2.0);
        return distanceSq < Math.pow(radius, 2.0) && (!hollow || distanceSq >= Math.pow(radius - 1.0f, 2.0));
    }

    private int getSlotInInventory() {
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING && !isSaveEnchanted(itemStack)) {
                return adjustSlotNumber(i);
            }
        }
        return -1;
    }

    private int adjustSlotNumber(int slot) {
        return (slot < 9) ? (slot + 36) : slot;
    }

    private boolean hasJackHeadInInventory() {
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.PLAYER_HEAD && stack.hasDisplayName()) {
                String displayName = stack.getDisplayName().getString();
                if ("Голова Джека".equals(displayName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onDisable() {
        oldItem = -1;
        totemIsUsed = false;
        return super.onDisable();
    }
}


