package polaroid.client.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import polaroid.client.Polaroid;
import polaroid.client.command.friends.FriendStorage;
import polaroid.client.events.EventInput;
import polaroid.client.events.EventMotion;
import polaroid.client.events.EventUpdate;
import polaroid.client.events.MovingEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.movement.ElytraTarget;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.SensUtils;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.player.InventoryUtil;
import polaroid.client.utils.player.MouseUtil;
import polaroid.client.utils.player.MoveUtils;
import polaroid.client.utils.rotation.Rotation;
import polaroid.client.utils.rotation.RotationHandler;
import polaroid.client.utils.rotation.RotationManager;
import polaroid.client.utils.rotation.RotationMath;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.hypot;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@Getter
@Setter
@ModuleSystem(name = "Aura", type = Category.Combat, server = ServerCategory.NO, description = "Автоматически атакует энтити")
public class Aura extends Module {

    private final ModeSetting aimMode = new ModeSetting("Наводка", "FunTime", 
            "FunTime", "SpookyTime", "SpookyTimeDuels", "ReallyWorld", "HollyWorld", "Простая", "Intave");

    private final SliderSetting attackRange = new SliderSetting("Дистанция удара", 3f, 1f, 6f, 0.1f);
    private final SliderSetting lookRange = new SliderSetting("Дополнительная дистанция", 1.5f, 0f, 2f, 0.1f);
    
    private final SliderSetting elytraRotateRange = new SliderSetting("Ротация на элитре", 12.5f, 0f, 64f, 0.5f)
            .setVisible(() -> aimMode.is("ReallyWorld"));
    private final SliderSetting elytraDistanceReduce = new SliderSetting("Элитра дистанция", 0.7f, 0f, 0.7f, 0.05f)
            .setVisible(() -> aimMode.is("ReallyWorld"));

    private final ModeListSetting targetType = new ModeListSetting("Тип таргета",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Мобы", false),
            new BooleanSetting("Животные", false),
            new BooleanSetting("Друзья", false),
            new BooleanSetting("Голые", true),
            new BooleanSetting("Невидимки", true),
            new BooleanSetting("Голые невидимки", true));

    private final ModeListSetting attackSetting = new ModeListSetting("Настройки",
            new BooleanSetting("Только криты", true),
            new BooleanSetting("Ломать щит", true),
            new BooleanSetting("Отжимать щит", true),
            new BooleanSetting("Не атаковать при еде", false),
            new BooleanSetting("Игнорировать стены", false),
            new BooleanSetting("Фокусировать одну цель", true));

    private final SliderSetting hitChance = new SliderSetting("Шанс удара в %", 100f, 1f, 100f, 1f);

    private final ModeSetting correctionType = new ModeSetting("Коррекция движения", "Свободная", 
            "Свободная", "Фокусированная", "Преследование");

    private final ModeSetting sprintReset = new ModeSetting("Сброс спринта", "Legit", "Legit", "Packet");

    private final BooleanSetting smartCrits = new BooleanSetting("Умные криты", false)
            .setVisible(() -> attackSetting.getValueByName("Только криты").get());

    private LivingEntity target;
    private final StopWatch stopWatch = new StopWatch();
    private Vector2f rotateVector = new Vector2f(0, 0);
    private final RotationManager rotationManager = new RotationManager();
    
    private int ticks = 0;
    private boolean isRotated;
    private float lastYaw, lastPitch;

    private final AutoPotion autoPotion;

    public Aura(AutoPotion autoPotion) {
        this.autoPotion = autoPotion;
        addSettings(aimMode, attackRange, lookRange, elytraRotateRange, elytraDistanceReduce, 
                targetType, attackSetting, hitChance, correctionType, sprintReset, smartCrits);
    }
    
    public Vector2f getRotateVector() {
        return rotateVector;
    }

    @Subscribe
    public void onInput(EventInput eventInput) {
        if (target != null && mc.player != null) {
            if (correctionType.is("Свободная")) {
                MoveUtils.fixMovement(eventInput, rotateVector.x);
            } else if (correctionType.is("Преследование")) {
                MoveUtils.fixMovementToTarget(eventInput, target, rotateVector);
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (attackSetting.getValueByName("Фокусировать одну цель").get()) {
            if (target != null && isValid(target)) {
                float distanceToTarget = mc.player.getDistance(target);
                if (distanceToTarget > 7.5f) {
                    updateTarget();
                }
            } else {
                updateTarget();
            }
        } else {
            updateTarget();
        }

        if (target != null && !(autoPotion.isState() && autoPotion.isActive())) {
            isRotated = false;
            
            if (shouldPlayerFalling() && stopWatch.hasTimeElapsed()) {
                if (Math.random() * 100 <= hitChance.get()) {
                    updateAttack();
                    ticks = 2;
                }
            }

            if (aimMode.is("SpookyTime") || aimMode.is("FunTime")) {
                updateRotation(ticks > 0, 180, 90);
                if (ticks > 0) {
                    ticks--;
                }
            } else {
                if (!isRotated) {
                    updateRotation(false, 80, 35);
                }
            }
        } else {
            stopWatch.setLastMS(0);
            reset();
        }
    }

    @Subscribe
    private void onMotion(EventMotion event) {
        if (target == null || (autoPotion.isState() && autoPotion.isActive())) return;

        float yaw = rotateVector.x;
        float pitch = rotateVector.y;

        event.setYaw(yaw);
        event.setPitch(pitch);
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = yaw;
        mc.player.rotationPitchHead = pitch;
    }

    @Subscribe
    private void onMoving(MovingEvent event) {
        // Зарезервировано для будущих функций
    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();
        float range = attackRange.get() + 0.253f + lookRange.get();

        float elytraRotateBonus = 0.0f;
        if (mc.player.isElytraFlying() && aimMode.is("ReallyWorld")) {
            elytraRotateBonus = elytraRotateRange.get();
        }

        ElytraTarget elytraTarget = Polaroid.getInstance().getFunctionRegistry().getElytraTarget();
        if (mc.player.isElytraFlying() && elytraTarget != null && elytraTarget.isState()) {
            range += elytraTarget.elytraFindRange.get();
        }
        
        range += elytraRotateBonus;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof LivingEntity living && isValid(living)) {
                if (mc.player.getDistance(living) <= range) {
                    targets.add(living);
                }
            }
        }

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        if (targets.size() == 1) {
            target = targets.get(0);
            return;
        }

        targets.sort(Comparator.comparingDouble(object -> {
            if (object instanceof PlayerEntity player) {
                return -getEntityArmor(player);
            }
            if (object instanceof LivingEntity base) {
                return -base.getTotalArmorValue();
            }
            return 0.0;
        }).thenComparing((object, object2) -> {
            double d2 = getEntityHealth((LivingEntity) object);
            double d3 = getEntityHealth((LivingEntity) object2);
            return Double.compare(d2, d3);
        }).thenComparing((object, object2) -> {
            double d2 = mc.player.getDistance((LivingEntity) object);
            double d3 = mc.player.getDistance((LivingEntity) object2);
            return Double.compare(d2, d3);
        }));

        target = targets.get(0);
    }

    private void updateRotation(boolean attack, float rotationYawSpeed, float rotationPitchSpeed) {
        Vector3d targetPos = target.getPositionVec();
        
        ElytraTarget elytraTarget = Polaroid.getInstance().getFunctionRegistry().getElytraTarget();
        if (mc.player.isElytraFlying() && target.isElytraFlying() && ElytraTarget.shouldElytraTarget) {
            Vector3d targetVelocity = target.getMotion();
            double targetSpeed = Math.sqrt(targetVelocity.x * targetVelocity.x + targetVelocity.z * targetVelocity.z);
            
            if (targetSpeed > 0.35 && elytraTarget != null) {
                float leadTicks = elytraTarget.elytraForward.get();
                targetPos = target.getPositionVec().add(targetVelocity.scale(leadTicks));
            }
        }

        Vector3d vec = targetPos.add(0, clamp(mc.player.getPosYEye() - target.getPosY(),
                        0, target.getHeight() * (mc.player.getDistanceEyePos(target) / attackRange.get())), 0)
                .subtract(mc.player.getEyePosition(1.0F));

        isRotated = true;

        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));

        float yawDelta = wrapDegrees(yawToTarget - rotateVector.x);
        float pitchDelta = wrapDegrees(pitchToTarget - rotateVector.y);

        String currentMode = aimMode.get();
        if (currentMode.equals("FunTime") || currentMode.equals("SpookyTime") || 
            currentMode.equals("SpookyTimeDuels") || currentMode.equals("ReallyWorld") || 
            currentMode.equals("HollyWorld") || currentMode.equals("Простая") || 
            currentMode.equals("Intave")) {
            
            RotationHandler handler = rotationManager.getRotation(currentMode);
            
            Rotation currentRotation = new Rotation(rotateVector.x, rotateVector.y);
            Rotation targetRotation = new Rotation(yawToTarget, pitchToTarget);
            
            Rotation newRotation = handler.limitAngleChange(currentRotation, targetRotation, vec, target);
            
            float yaw = newRotation.getYaw();
            float pitch = clamp(newRotation.getPitch(), -89.0F, 89.0F);
            
            float gcd = SensUtils.getGCDValue();
            yaw -= (yaw - rotateVector.x) % gcd;
            pitch -= (pitch - rotateVector.y) % gcd;
            
            rotateVector = new Vector2f(yaw, pitch);
            
            mc.player.rotationYawOffset = yaw;
        } else {
            float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1.0f), rotationYawSpeed);
            float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1.0f), rotationPitchSpeed);

            if (attack) {
                clampedPitch = Math.max(Math.abs(pitchDelta), 1.0f);
            } else {
                clampedPitch /= 3f;
            }

            if (Math.abs(clampedYaw - this.lastYaw) <= 3.0f) {
                clampedYaw = this.lastYaw + 3.1f;
            }

            float yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
            float pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);

            float gcd = SensUtils.getGCDValue();
            yaw -= (yaw - rotateVector.x) % gcd;
            pitch -= (pitch - rotateVector.y) % gcd;

            rotateVector = new Vector2f(yaw, pitch);
            lastYaw = clampedYaw;
            lastPitch = clampedPitch;
            
            mc.player.rotationYawOffset = yaw;
        }
    }

    private void updateAttack() {
        float effectiveRange = attackRange.get();
        
        if (mc.player.isElytraFlying() && aimMode.is("ReallyWorld")) {
            float elytraSpeed = (float) Math.sqrt(
                mc.player.getMotion().x * mc.player.getMotion().x + 
                mc.player.getMotion().z * mc.player.getMotion().z
            );
            float distanceReduction = elytraDistanceReduce.get() - elytraSpeed * 0.1f;
            effectiveRange -= distanceReduction;
        }
        
        Entity selected = MouseUtil.getMouseOver(target, rotateVector.x, rotateVector.y, effectiveRange);

        if ((selected == null || selected != target) && !mc.player.isElytraFlying()) {
            return;
        }

        if (mc.player.isBlocking() && attackSetting.getValueByName("Отжимать щит").get()) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }

        // Сброс спринта
        if (mc.player.isSprinting()) {
            if (sprintReset.is("Packet")) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
            }
        }

        stopWatch.setLastMS(500);
        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);
        
        // Update FunTime rotation attack state
        polaroid.client.utils.rotation.impl.FunTimeRotation.updateAttackState(true);

        if (target instanceof PlayerEntity player && attackSetting.getValueByName("Ломать щит").get()) {
            breakShieldPlayer(player);
        }
    }

    private boolean shouldPlayerFalling() {
        boolean cancelReason = mc.player.isInWater() && mc.player.areEyesInFluid(FluidTags.WATER) || 
                mc.player.isInLava() || mc.player.isOnLadder() || 
                mc.player.isPassenger() || mc.player.abilities.isFlying;

        float attackStrength = mc.player.getCooledAttackStrength(
                Polaroid.getInstance().getTpsCalc().getAdjustTicks());

        if (attackStrength < 0.92f) {
            return false;
        }

        if (!cancelReason && attackSetting.getValueByName("Только криты").get()) {
            if (smartCrits.get()) {
                // Умные криты: если не прыгаем - бьем на земле, если прыгаем - даем криты
                boolean isJumping = mc.gameSettings.keyBindJump.isKeyDown();
                
                if (!isJumping) {
                    // Не прыгаем - бьем просто по кулдауну (на земле или в воздухе)
                    return true;
                } else {
                    // Прыгаем - даем криты (только когда падаем)
                    boolean isFalling = !mc.player.isOnGround() && mc.player.fallDistance > 0;
                    boolean hasDownwardMotion = mc.player.getMotion().y < 0;
                    return isFalling && hasDownwardMotion;
                }
            }
            
            // Обычные криты - только когда падаем
            boolean isFalling = !mc.player.isOnGround() && mc.player.fallDistance > 0;
            boolean hasDownwardMotion = mc.player.getMotion().y < 0;
            return isFalling && hasDownwardMotion;
        }

        return true;
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;
        if (entity.ticksExisted < 3) return false;

        if (entity instanceof PlayerEntity p) {
            if (AntiBot.isBot(entity)) return false;
            
            if (!targetType.getValueByName("Друзья").get() && FriendStorage.isFriend(p.getName().getString())) {
                return false;
            }
            
            if (p.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) return false;
        }

        if (entity instanceof PlayerEntity && !targetType.getValueByName("Игроки").get()) return false;
        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !targetType.getValueByName("Голые").get()) return false;
        if (entity instanceof PlayerEntity && entity.isInvisible() && entity.getTotalArmorValue() == 0 && !targetType.getValueByName("Голые невидимки").get()) return false;
        if (entity instanceof PlayerEntity && entity.isInvisible() && !targetType.getValueByName("Невидимки").get()) return false;
        if (entity instanceof MonsterEntity && !targetType.getValueByName("Мобы").get()) return false;
        if (entity instanceof AnimalEntity && !targetType.getValueByName("Животные").get()) return false;

        // Проверка на стены
        if (!attackSetting.getValueByName("Игнорировать стены").get()) {
            if (!mc.player.canEntityBeSeen(entity)) {
                return false;
            }
        }

        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private void breakShieldPlayer(PlayerEntity entity) {
        if (entity.isBlocking()) {
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

    private void reset() {
        mc.player.rotationYawOffset = Integer.MIN_VALUE;
        rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    private double getEntityArmor(PlayerEntity entityPlayer2) {
        double d2 = 0.0;
        for (int i2 = 0; i2 < 4; ++i2) {
            ItemStack is = entityPlayer2.inventory.armorInventory.get(i2);
            if (!(is.getItem() instanceof ArmorItem)) continue;
            d2 += getProtectionLvl(is);
        }
        return d2;
    }

    private double getProtectionLvl(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem i) {
            double damageReduceAmount = i.getDamageReduceAmount();
            if (stack.isEnchanted()) {
                damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25;
            }
            return damageReduceAmount;
        }
        return 0;
    }

    private double getEntityHealth(LivingEntity ent) {
        if (ent instanceof PlayerEntity player) {
            return (double) (player.getHealth() + player.getAbsorptionAmount()) * (getEntityArmor(player) / 20.0);
        }
        return ent.getHealth() + ent.getAbsorptionAmount();
    }

    @Override
    public boolean onEnable() {
        super.onEnable();
        reset();
        target = null;
        return false;
    }

    @Override
    public boolean onDisable() {
        super.onDisable();
        reset();
        stopWatch.setLastMS(0);
        target = null;
        return false;
    }
}


