package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.Polaroid;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;

@ModuleSystem(name = "ElytraTarget", type = Category.Movement, server = ServerCategory.NO, description = "Помогает летать за целью на элитрах и использовать фейерверки")
public class ElytraTarget extends Module {

    private final BooleanSetting swapVector = new BooleanSetting("Свап вектора", false);
    private final BooleanSetting autoFirework = new BooleanSetting("Auto Firework", true);
    private final SliderSetting fireworkDelay = new SliderSetting("Firework Delay", 1000.0f, 100.0f, 3000.0f, 100.0f);
    private final BooleanSetting autoJump = new BooleanSetting("Auto Jump", true);
    
    // Поля для совместимости с Aura
    public final SliderSetting elytraFindRange = new SliderSetting("Дистанция наводки", 32.0f, 6.0f, 64.0f, 1.0f);
    public final SliderSetting elytraForward = new SliderSetting("Значение перегона", 3.0f, 0.0f, 6.0f, 0.1f);
    
    public static boolean shouldElytraTarget = false;

    private long lastFireworkTime;
    private int jumpTicks = 0;

    public ElytraTarget() {
        addSettings(swapVector, autoFirework, fireworkDelay, autoJump, elytraFindRange, elytraForward);
    }

    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (mc.player == null || !this.isState()) {
            shouldElytraTarget = false;
            return;
        }

        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        boolean hasTarget = aura != null && aura.isState() && aura.getTarget() != null;
        boolean hasElytra = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA;

        // Устанавливаем флаг для использования в Aura
        shouldElytraTarget = hasTarget && hasElytra && mc.player.isElytraFlying();

        if (hasTarget && hasElytra && !mc.player.isElytraFlying() && autoJump.get()) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
                jumpTicks = 0;
            } else if (mc.player.getMotion().y > -0.1) { // Grim bypass jump
                jumpTicks++;
                if (jumpTicks == 1) {
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                }
            }
        }

        if (autoFirework.get() && mc.player.isElytraFlying()) {
            long delay = Float.valueOf(fireworkDelay.get()).longValue();
            if (System.currentTimeMillis() - lastFireworkTime >= delay) {
                ItemStack offhand = mc.player.getHeldItemOffhand();
                if (offhand.getItem() == Items.FIREWORK_ROCKET) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                    mc.player.swingArm(Hand.OFF_HAND);
                    lastFireworkTime = System.currentTimeMillis();
                }
            }
        }
    }

    public boolean isDefensiveActive() {
        return this.isState();
    }

    public BooleanSetting getSwapVector() {
        return swapVector;
    }

    @Override
    public boolean onDisable() {
        jumpTicks = 0;
        shouldElytraTarget = false;
        super.onDisable();
        return false;
    }
}


