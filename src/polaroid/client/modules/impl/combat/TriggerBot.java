package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.Polaroid;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.player.AttackUtil;
import polaroid.client.utils.player.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

@ModuleSystem(name = "TriggerBot", type = Category.Combat, server = ServerCategory.NO, description = "Автоматически атакует энтити при наводке")
public class TriggerBot extends Module {

    private static final float RANGE_MARGIN = 0.253F;
    private final Random random = new Random();
    private final StopWatch stopWatch = new StopWatch();
    
    public LivingEntity target;

    private final SliderSetting attackRange = new SliderSetting("Дистанция удара", 3, 1, 6, 0.1f);
    
    private final ModeListSetting targetType = new ModeListSetting("Тип таргета",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Мобы", true),
            new BooleanSetting("Животные", true),
            new BooleanSetting("Друзья", false),
            new BooleanSetting("Голые", false),
            new BooleanSetting("Невидимые", false)
    );

    private final ModeListSetting attackSettings = new ModeListSetting("Настройки атаки",
            new BooleanSetting("Только криты", true),
            new BooleanSetting("Ломать щит", true),
            new BooleanSetting("Не бить с едой", true),
            new BooleanSetting("Игнорировать стены", false),
            new BooleanSetting("Шанс удара", false)
    );

    private final SliderSetting hitChance = new SliderSetting("Шанс удара %", 100, 1, 100, 1)
            .setVisible(() -> attackSettings.getValueByName("Шанс удара").get());

    private final ModeSetting sprintReset = new ModeSetting("Сброс спринта", "Legit", "Legit", "Packet");

    private final BooleanSetting smartCrits = new BooleanSetting("Умные криты", true)
            .setVisible(() -> attackSettings.getValueByName("Только криты").get());

    public TriggerBot() {
        addSettings(attackRange, targetType, attackSettings, hitChance, sprintReset, smartCrits);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Обновляем цель
        target = updateTarget();

        if (target == null) {
            return;
        }

        // Проверяем условия для атаки
        if (shouldAttack()) {
            performAttack();
        }
    }

    private LivingEntity updateTarget() {
        // Проверяем rayTrace на энтити
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
            
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                
                // Проверяем валидность цели
                if (isValidTarget(livingEntity)) {
                    float range = attackRange.get() + RANGE_MARGIN;
                    float distance = mc.player.getDistance(livingEntity);
                    
                    if (distance <= range) {
                        return livingEntity;
                    }
                }
            }
        }
        
        return null;
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (!entity.isAlive()) {
            return false;
        }

        // Проверяем тип энтити
        AttackUtil entitySelector = new AttackUtil();

        if (targetType.getValueByName("Игроки").get()) {
            entitySelector.apply(AttackUtil.EntityType.PLAYERS);
        }
        if (targetType.getValueByName("Мобы").get()) {
            entitySelector.apply(AttackUtil.EntityType.MOBS);
        }
        if (targetType.getValueByName("Животные").get()) {
            entitySelector.apply(AttackUtil.EntityType.ANIMALS);
        }

        if (entitySelector.ofType(entity, entitySelector.build()) == null) {
            return false;
        }

        // Проверка на друзей
        if (!targetType.getValueByName("Друзья").get() && entity instanceof PlayerEntity) {
            if (polaroid.client.command.friends.FriendStorage.isFriend(entity.getName().getString())) {
                return false;
            }
        }

        // Проверка на голых
        if (!targetType.getValueByName("Голые").get() && entity instanceof PlayerEntity) {
            if (isNaked((PlayerEntity) entity)) {
                return false;
            }
        }

        // Проверка на невидимых
        if (!targetType.getValueByName("Невидимые").get() && entity.isInvisible()) {
            return false;
        }

        return true;
    }

    private boolean isNaked(PlayerEntity player) {
        return player.inventory.armorInventory.stream().allMatch(stack -> stack.isEmpty());
    }

    private boolean shouldAttack() {
        if (target == null) {
            return false;
        }

        // Проверка на шанс удара
        if (attackSettings.getValueByName("Шанс удара").get()) {
            float chance = hitChance.get();
            if (random.nextFloat() * 100 > chance) {
                return false;
            }
        }

        // Проверка на еду
        if (attackSettings.getValueByName("Не бить с едой").get()) {
            if (mc.player.isHandActive() && mc.player.getActiveItemStack().isFood()) {
                return false;
            }
        }

        // Проверка на криты
        if (attackSettings.getValueByName("Только криты").get()) {
            boolean smartCrit = smartCrits.get();
            if (!AttackUtil.isPlayerFalling(true, smartCrit, false)) {
                return false;
            }
        }

        // Проверка кулдауна атаки
        if (mc.player.getCooledAttackStrength(0.5f) < 0.92f) {
            return false;
        }

        // Проверка таймера
        if (!stopWatch.hasTimeElapsed()) {
            return false;
        }

        return true;
    }

    private void performAttack() {
        if (target == null || mc.player == null) {
            return;
        }

        boolean shouldStopSprinting = false;

        // Сброс спринта
        if (sprintReset.is("Packet")) {
            if (CEntityActionPacket.lastUpdatedSprint) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                shouldStopSprinting = true;
            }
        } else if (sprintReset.is("Legit")) {
            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
        }

        // Атака
        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);

        // Ломаем щит
        if (attackSettings.getValueByName("Ломать щит").get() && target instanceof PlayerEntity) {
            breakShieldPlayer(target);
        }

        // Восстанавливаем спринт
        if (shouldStopSprinting) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
        }

        // Устанавливаем задержку
        stopWatch.setLastMS(500);
    }

    private void breakShieldPlayer(Entity entity) {
        if (((LivingEntity) entity).isBlocking()) {
            int invSlot = InventoryUtil.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryUtil.getInstance().getAxeInInventory(true);

            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryUtil.getInstance().findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);

                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));

                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
            }

            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }
    }

    @Override
    public boolean onDisable() {
        stopWatch.reset();
        target = null;
        super.onDisable();
        return false;
    }
}


