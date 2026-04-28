package polaroid.client.modules.impl.movement;

import polaroid.client.events.*;
import polaroid.client.modules.impl.combat.Aura;
import com.google.common.eventbus.Subscribe;

import polaroid.client.Polaroid;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.player.DamagePlayerUtil;
import polaroid.client.utils.player.InventoryUtil;
import polaroid.client.utils.player.MoveUtils;
import polaroid.client.utils.player.StrafeMovement;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;

import java.util.Random;


@ModuleSystem(name = "Strafe", type = Category.Movement, server = ServerCategory.NO, description = "Дефолт стрейфы")
public class Strafe extends Module {
    private final ModeSetting mode = new ModeSetting("Обход", "Matrix Hard", "Matrix", "Matrix Hard");
    private final BooleanSetting elytra = new BooleanSetting("Буст с элитрой", false);
    private final SliderSetting setSpeed = new SliderSetting("Скорость", 1.5F, 0.5F, 2.5F, 0.1F).setVisible(() -> elytra.get());
    private final BooleanSetting damageBoost = new BooleanSetting("Буст с дамагом", false);
    private final SliderSetting boostSpeed = new SliderSetting("Значение буста", 0.7f, 0.1F, 5.0f, 0.1F).setVisible(() -> damageBoost.get());

    private final BooleanSetting onlyGround = new BooleanSetting("Только на земле", false).setVisible(() -> mode.is("Matrix Hard"));
    private final BooleanSetting autoJump = new BooleanSetting("Прыгать", false);
    private final BooleanSetting moveDir = new BooleanSetting("Направление", true);

    private final DamagePlayerUtil damageUtil = new DamagePlayerUtil();
    private final StrafeMovement strafeMovement = new StrafeMovement();
    public static int waterTicks;

    public boolean check() {
        return Polaroid.getInstance().getFunctionRegistry().getAura().getTarget() != null && Polaroid.getInstance().getFunctionRegistry().getAura().isState();
    }

    public Strafe() {
        addSettings(mode, elytra, setSpeed, damageBoost, boostSpeed, onlyGround, autoJump, moveDir);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        if (mode.is("Grim")) return;
        handleEventAction(e);
    }

    @Subscribe
    private void onMoving(MovingEvent e) {
        if (mode.is("Grim")) return;
        handleEventMove(e);
    }

    @Subscribe
    private void onPostMove(PostMoveEvent e) {
        if (mode.is("Grim")) return;
        handleEventPostMove(e);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (mode.is("Grim")) return;
        handleEventPacket(e);
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        if (mode.is("Grim")) return;
        handleDamageEvent(e);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (moveDir.get() && !check()) {
            float yaw = mc.player.rotationYaw;
            if (mc.player.moveForward < 0F) yaw += 180F;
            float forward = 1F;
            if (mc.player.moveForward < 0F) forward = -0.5F;
            else if (mc.player.moveForward > 0F) forward = 0.5F;
            if (mc.player.moveStrafing > 0F) yaw -= 90F * forward;
            if (mc.player.moveStrafing < 0F) yaw += 90F * forward;
            
            mc.player.rotationYawHead = yaw;
            mc.player.renderYawOffset = yaw;
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (autoJump.get()) {
            if (mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
                mc.player.jump();
            }
        }

        if (!elytra.get()) return;
        int elytraSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ELYTRA, true);

        if (mc.player.isInWater() || mc.player.isInLava() || waterTicks > 0 || elytraSlot == -1)
            return;
        if (mc.player.fallDistance != 0 && mc.player.fallDistance < 0.1 && mc.player.motion.y < -0.1) {
            if (elytraSlot != -2) {
                mc.playerController.windowClick(0, elytraSlot, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
            }
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));

            if (elytraSlot != -2) {
                mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, elytraSlot, 1, ClickType.PICKUP, mc.player);
            }
        }
    }

    private void handleDamageEvent(EventDamageReceive damage) {
        if (damageBoost.get()) {
            damageUtil.processDamage(damage);
        }
    }

    private void handleEventAction(ActionEvent action) {
        if  (mode.is("Matrix Hard")) {
            if (strafes()) {
                handleStrafesEventAction(action);
            }
            if (strafeMovement.isNeedSwap()) {
                handleNeedSwapEventAction(action);
            }
        }
    }

    private void handleEventMove(MovingEvent eventMove) {
        int elytraSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ELYTRA, true);

        if (elytra.get() && elytraSlot != -1) {
            if (MoveUtils.isMoving() && !mc.player.isOnGround() && mc.player.fallDistance >= 0.15 && eventMove.isToGround()) {
                double yaw = Math.toRadians(mc.player.rotationYaw);
                if (mc.player.moveForward < 0F) yaw += Math.PI;
                float forward = 1F;
                if (mc.player.moveForward < 0F) forward = -0.5F;
                else if (mc.player.moveForward > 0F) forward = 0.5F;
                if (mc.player.moveStrafing > 0F) yaw -= Math.PI / 2 * forward;
                if (mc.player.moveStrafing < 0F) yaw += Math.PI / 2 * forward;
                
                double speed = setSpeed.get();
                mc.player.setMotion(-Math.sin(yaw) * speed, mc.player.motion.y, Math.cos(yaw) * speed);
                strafeMovement.setOldSpeed(setSpeed.get() / 1.06);
            }
        }

        if (mc.player.isInWater() || mc.player.isInLava()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
        if (mode.is("Matrix Hard")) {
            if (onlyGround.get())
                if (!mc.player.isOnGround()) return;

            if (strafes()) {
                handleStrafesEventMove(eventMove);
            } else {
                strafeMovement.setOldSpeed(0);
            }
        }

        if (mode.is("Matrix")) {
            if (waterTicks > 0) return;
            double motion = Math.hypot(mc.player.getMotion().x, mc.player.getMotion().z);
            if (MoveUtils.isMoving() && motion <= 0.289385188) {
                if (!eventMove.isToGround()) {
                    double speed = mc.player.isHandActive() ? motion - 0.00001f : 0.245f - (new Random().nextFloat() * 0.000001f);
                    
                    double yaw = Math.toRadians(mc.player.rotationYaw);
                    if (mc.player.moveForward < 0F) yaw += Math.PI;
                    float forward = 1F;
                    if (mc.player.moveForward < 0F) forward = -0.5F;
                    else if (mc.player.moveForward > 0F) forward = 0.5F;
                    if (mc.player.moveStrafing > 0F) yaw -= Math.PI / 2 * forward;
                    if (mc.player.moveStrafing < 0F) yaw += Math.PI / 2 * forward;
                    
                    mc.player.setMotion(-Math.sin(yaw) * speed, mc.player.motion.y, Math.cos(yaw) * speed);
                }
            }
        }
    }

    private void handleEventPostMove(PostMoveEvent eventPostMove) {
        strafeMovement.postMove(eventPostMove.getHorizontalMove());
    }

    private void handleEventPacket(EventPacket packet) {

        if (packet.getType() == EventPacket.Type.RECEIVE) {
            if (damageBoost.get()) {
                damageUtil.onPacketEvent(packet);
            }
            handleReceivePacketEventPacket(packet);
        }
    }

    private void handleStrafesEventAction(ActionEvent action) {
        if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
            action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        }
    }

    private void handleStrafesEventMove(MovingEvent eventMove) {


        if (damageBoost.get())
            this.damageUtil.time(700L);

        final float damageSpeed = boostSpeed.get().floatValue() / 10.0F;
        final double speed = strafeMovement.calculateSpeed(eventMove, damageBoost.get(), damageUtil.isNormalDamage(), false, damageSpeed);

        MoveUtils.MoveEvent.setMoveMotion(eventMove, speed);
    }

    private void handleNeedSwapEventAction(ActionEvent action) {
        action.setSprintState(!mc.player.serverSprintState);
        strafeMovement.setNeedSwap(false);
    }

    private void handleReceivePacketEventPacket(EventPacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }

    }

    public boolean strafes() {
        if (isInvalidPlayerState()) {
            return false;
        }

        if (mc.player.isInWater() || waterTicks > 0) {
            return false;
        }

        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();

        if (isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
        }

        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }

        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();

        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }

    @Override
    public boolean onEnable() {
        strafeMovement.setOldSpeed(0);
        super.onEnable();
        return false;
    }
}


