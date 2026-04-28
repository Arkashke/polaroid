package polaroid.client.modules.impl.movement;

import polaroid.client.Polaroid;
import polaroid.client.events.EventMotion;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.player.InventoryUtil;
import polaroid.client.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;

@ModuleSystem(
        name = "ElytraJump",
        type = Category.Movement,
        server = ServerCategory.NO,
        description = "Поднимает вас в верх с авто-взлётом и прыжком"
)
public class ElytraJump extends Module {

    private final BooleanSetting auto = new BooleanSetting("Умный свап", false);
    private final StopWatch timer = new StopWatch();
    private boolean hasFiredOnStart = false;

    public ElytraJump() {
        this.addSettings(auto);
    }

    private int getItemSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
                break;
            }
        }
        if (finalSlot < 9 && finalSlot != -1) {
            finalSlot += 36;
        }
        return finalSlot;
    }

    private int getChestPlateSlot() {
        Item[] items = {
                Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE,
                Items.IRON_CHESTPLATE, Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE
        };

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

    @Subscribe
    public void onUpdate(EventUpdate event) {
        // === Авто-прыжок с земли ===
        if (!mc.player.abilities.isFlying
                && mc.player.isOnGround()
                && !mc.player.isInWater()
                && !mc.player.isInLava()
                && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA
                && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.jump();
        }

        // === Авто-взлёт в воздухе ===
        if (!mc.player.abilities.isFlying
                && !mc.player.isOnGround()
                && !mc.player.isInWater()
                && !mc.player.isElytraFlying()
                && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {

            mc.player.startFallFlying();
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
        }

        if (mc.player.isOnGround() || mc.player.isInWater() || mc.player.isInLava()) {
            hasFiredOnStart = false;
        }

        if (mc.player.hurtTime > 0 && auto.get()
                && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            swapToChestplate();
            return;
        }

        if (mc.player.movementInput != null
                && getItemSlot(Items.ELYTRA) == 38
                && mc.player.movementInput.jump
                && !mc.player.isElytraFlying()) {
            mc.player.movementInput.jump = false;
        }

        if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            MoveUtils.setMotion(0);
            mc.gameSettings.keyBindJump.setPressed(true);

            if (mc.player.isElytraFlying()) {
                mc.player.getMotion().y += 0.06f + (float)(Math.random() * 0.001f);
            }
        } else if (auto.get()) {
            swapToElytra();
        }
    }

    @Subscribe
    public void onMotion(EventMotion motionEvent) {
        if (mc.player.isElytraFlying()) {
            Aura killAura = Polaroid.getInstance().getFunctionRegistry().getAura();
            float targetYaw = killAura.getTarget() != null ? killAura.getRotateVector().x : mc.player.rotationYaw;
            float targetPitch = 0.0f;

            motionEvent.setYaw(targetYaw);
            motionEvent.setPitch(targetPitch);

            mc.player.rotationYaw = targetYaw;
            mc.player.rotationPitch = targetPitch;
            mc.player.rotationYawHead = targetYaw;
            mc.player.renderYawOffset = targetYaw;
        }
    }

    @Override
    public boolean onEnable() {
        hasFiredOnStart = false;
        if (auto.get()) {
            swapToElytra();
        }
        return super.onEnable();
    }

    @Override
    public boolean onDisable() {
        if (auto.get() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            swapToChestplate();
        }
        return super.onDisable();
    }

    private void swapToElytra() {
        int elytraSlot = getItemSlot(Items.ELYTRA);
        if (elytraSlot != -1) {
            InventoryUtil.moveItem(elytraSlot, 6);
        }
    }

    private void swapToChestplate() {
        int chestplateSlot = getChestPlateSlot();
        if (chestplateSlot != -1) {
            InventoryUtil.moveItem(chestplateSlot, 6);
        }
    }
}


