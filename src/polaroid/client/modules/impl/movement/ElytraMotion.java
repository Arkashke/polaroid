package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.Polaroid;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.modules.impl.misc.ElytraHelper;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.performance.ModuleThrottler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

@ModuleSystem(
        name = "ElytraMotion",
        type = Category.Movement,
        server = ServerCategory.NO,
        description = "Позволяет зависнуть возле цели на элитрах"
)
public class ElytraMotion extends Module {

    public final SliderSetting distance = new SliderSetting("Дистанция", 1.5F, 1.0F, 3.0F, 0.1F);
    
    public final BooleanSetting autoFirework = new BooleanSetting("АвтоФейрверк", true);
    
    private final StopWatch fireworkTimer = new StopWatch();

    public ElytraMotion() {
        addSettings(distance, autoFirework);
    }

    @Subscribe
    private void onUpdate(EventUpdate event) {
        // Оптимизация: проверяем базовые условия сразу
        if (mc.player == null || mc.world == null || !mc.player.isElytraFlying()) return;
        
        // Оптимизация: throttling - выполняем каждый 2-й тик для экономии ресурсов
        if (!ModuleThrottler.getInstance().shouldExecute("ElytraMotion", 2)) return;
        
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        if (aura == null) return;
        
        LivingEntity target = aura.getTarget();
        if (target == null) return;
        
        // Оптимизация: кэшируем значение дистанции
        double distanceToTarget = mc.player.getDistanceEyePos(target);
        
        // Проверяем что цель близко и Aura не блокирует позицию
        if (distanceToTarget < distance.get() && !isAuraBlocking()) {
            // Останавливаем движение
            mc.player.setMotion(Vector3d.ZERO);
            
            // Автоматически используем фейерверк если включено
            if (autoFirework.get() && fireworkTimer.hasTimeElapsed(1000L)) {
                useFirework();
                fireworkTimer.reset();
            }
        }
    }
    
    private boolean isAuraBlocking() {
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        if (aura == null || !aura.isState()) return false;
        
        // Проверяем есть ли поле blockPos в Aura (из QuickProject логики)
        // В polaroid нет такого поля, поэтому просто возвращаем false
        return false;
    }
    
    private void useFirework() {
        ElytraHelper elytraHelper = Polaroid.getInstance().getFunctionRegistry().getElytraHelper();
        if (elytraHelper != null && elytraHelper.isState()) {
            polaroid.client.utils.player.InventoryUtil inventoryUtil = polaroid.client.utils.player.InventoryUtil.getInstance();
            int hbSlot = inventoryUtil.getSlotInInventoryOrHotbar(net.minecraft.item.Items.FIREWORK_ROCKET, true);
            int invSlot = inventoryUtil.getSlotInInventoryOrHotbar(net.minecraft.item.Items.FIREWORK_ROCKET, false);
            
            if (invSlot == -1 && hbSlot == -1) {
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(net.minecraft.item.Items.FIREWORK_ROCKET)) {
                if (hbSlot != -1) {
                    mc.player.connection.sendPacket(new net.minecraft.network.play.client.CHeldItemChangePacket(hbSlot));
                    mc.player.connection.sendPacket(new net.minecraft.network.play.client.CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                    mc.player.connection.sendPacket(new net.minecraft.network.play.client.CHeldItemChangePacket(mc.player.inventory.currentItem));
                    mc.player.swingArm(net.minecraft.util.Hand.MAIN_HAND);
                } else if (invSlot != -1) {
                    mc.playerController.pickItem(invSlot);
                    mc.player.connection.sendPacket(new net.minecraft.network.play.client.CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                    mc.player.swingArm(net.minecraft.util.Hand.MAIN_HAND);
                }
            }
        }
    }
}


