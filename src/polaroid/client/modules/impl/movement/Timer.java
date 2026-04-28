package polaroid.client.modules.impl.movement;

import polaroid.client.events.EventKey;
import polaroid.client.events.EventMotion;
import polaroid.client.events.EventPacket;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.StopWatch;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.MathHelper;

@ModuleSystem(
        name = "Timer",
        type = Category.Movement,
        server = ServerCategory.NO,
        description = "Изменяет скорость игры"
)
public class Timer extends Module {
    public ModeSetting mode = new ModeSetting("Режим", "Обычный", "Обычный", "Grim");
    public BindSetting grimBind = new BindSetting("Кнопка буста", 0).setVisible(() -> mode.is("Grim"));
    public SliderSetting timerAmount = new SliderSetting("Скорость", 2.0F, 1.0F, 5.0F, 0.025F);
    public BooleanSetting smart = new BooleanSetting("Умный", true).setVisible(() -> !mode.is("Grim"));
    public BooleanSetting movingUp = new BooleanSetting("Добавлять в движении", false).setVisible(() -> !mode.is("Grim"));
    public SliderSetting upValue = new SliderSetting("Значение", 0.02F, 0.01F, 0.5F, 0.01F).setVisible(() -> movingUp.get());
    public SliderSetting ticks = new SliderSetting("Скорость убывания", 1.0F, 0.15F, 3.0F, 0.1F).setVisible(() -> !mode.is("Grim"));
    
    public float maxViolation = 100.0F;
    private float violation = 0.0F;
    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;
    private float yaw;
    private float pitch;
    public float animWidth;
    private boolean isBoost;
    private StopWatch timerUtil = new StopWatch();

    public Timer() {
        addSettings(mode, timerAmount, grimBind, smart, movingUp, upValue, ticks);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == grimBind.get()) {
            isBoost = true;
        }
    }

    @Subscribe
    public void onMotion(EventMotion eventPre) {
        if (mode.is("Grim")) {
            updateTimer(eventPre.getYaw(), eventPre.getPitch(), eventPre.getX(), eventPre.getY(), eventPre.getZ());
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        handleEventUpdate();
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        handlePacketEvent(e);
    }

    private void handlePacketEvent(EventPacket e) {
        if (mode.is("Grim")) {
            if (e.isReceive()) {
                isFlagged(e);
                isDamaged(e);
            }

            if (e.isSend()) {
                cancelTransaction(e);
            }
        }
    }

    private void cancelTransaction(EventPacket e) {
        IPacket<?> packet = e.getPacket();
        if (packet instanceof CConfirmTransactionPacket) {
            e.cancel();
        }
    }

    private void isDamaged(EventPacket e) {
        IPacket<?> packet = e.getPacket();
        if (packet instanceof SEntityVelocityPacket) {
            SEntityVelocityPacket p = (SEntityVelocityPacket) packet;
            if (p.getEntityID() == mc.player.getEntityId()) {
                reset();
                resetSpeed();
            }
        }
    }

    private void isFlagged(EventPacket e) {
        IPacket<?> packet = e.getPacket();
        if (packet instanceof SPlayerPositionLookPacket) {
            if (isBoost) {
                resetSpeed();
                reset();
            }
        }
    }

    private void handleEventUpdate() {
        if (timerUtil.isReached(25000L)) {
            reset();
            timerUtil.reset();
        }

        if (!mc.player.isOnGround() && !isBoost) {
            violation += 0.1F;
            violation = MathHelper.clamp(violation, 0.0F, maxViolation / (mode.is("Grim") ? 1.0F : timerAmount.get()));
        }

        if (!mode.is("Grim") || isBoost) {
            mc.timer.timerSpeed = timerAmount.get();
            if (smart.get() && !(mc.timer.timerSpeed <= 1.0F)) {
                if (violation < maxViolation / timerAmount.get()) {
                    violation += mode.is("Grim") ? 0.05F : ticks.get();
                    violation = MathHelper.clamp(violation, 0.0F, maxViolation / (mode.is("Grim") ? 1.0F : timerAmount.get()));
                } else {
                    resetSpeed();
                }
            }
        }
    }

    public void updateTimer(float yaw, float pitch, double posX, double posY, double posZ) {
        if (notMoving()) {
            if (mode.is("Grim")) {
                violation = violation - 0.05F;
            } else {
                violation = violation - (ticks.get() + 0.4F);
            }
        } else if (movingUp.get() && !mode.is("Grim")) {
            violation -= upValue.get();
        }

        violation = MathHelper.clamp(violation, 0.0F, (float) Math.floor(maxViolation));
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    private boolean notMoving() {
        return prevPosX == mc.player.getPosX()
                && prevPosY == mc.player.getPosY()
                && prevPosZ == mc.player.getPosZ()
                && yaw == mc.player.rotationYaw
                && pitch == mc.player.rotationPitch;
    }

    public float getViolation() {
        return violation;
    }

    public void resetSpeed() {
        mc.timer.timerSpeed = 1.0F;
    }

    public void reset() {
        if (mode.is("Grim")) {
            violation = maxViolation / timerAmount.get();
            isBoost = false;
        }
    }

    @Override
    public boolean onDisable() {
        reset();
        mc.timer.timerSpeed = 1.0F;
        timerUtil.reset();
        return super.onDisable();
    }

    @Override
    public boolean onEnable() {
        reset();
        mc.timer.timerSpeed = 1.0F;
        return super.onEnable();
    }
}


