package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "Speed", type = Category.Movement, server = ServerCategory.NO, description = "Увеличивает скорость передвижения")
public class Speed extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Vanilla", "Vanilla", "Strafe", "Bhop");
    private final SliderSetting speed = new SliderSetting("Скорость", 1.0f, 0.1f, 3.0f, 0.1f);

    public Speed() {
        addSettings(mode, speed);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        switch (mode.get()) {
            case "Vanilla":
                applyVanillaSpeed();
                break;
            case "Strafe":
                applyStrafeSpeed();
                break;
            case "Bhop":
                applyBhopSpeed();
                break;
        }
    }

    private void applyVanillaSpeed() {
        if (mc.player.isOnGround() && (mc.player.moveForward != 0 || mc.player.moveStrafing != 0)) {
            float speedValue = speed.get();
            mc.player.setMotion(
                mc.player.getMotion().x * speedValue,
                mc.player.getMotion().y,
                mc.player.getMotion().z * speedValue
            );
        }
    }

    private void applyStrafeSpeed() {
        if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
            float yaw = mc.player.rotationYaw;
            float forward = mc.player.moveForward;
            float strafe = mc.player.moveStrafing;
            
            float speedValue = speed.get() * 0.3f;
            
            double motionX = -Math.sin(Math.toRadians(yaw)) * forward * speedValue + Math.cos(Math.toRadians(yaw)) * strafe * speedValue;
            double motionZ = Math.cos(Math.toRadians(yaw)) * forward * speedValue + Math.sin(Math.toRadians(yaw)) * strafe * speedValue;
            
            mc.player.setMotion(motionX, mc.player.getMotion().y, motionZ);
        }
    }

    private void applyBhopSpeed() {
        if (mc.player.isOnGround() && (mc.player.moveForward != 0 || mc.player.moveStrafing != 0)) {
            mc.player.jump();
            float speedValue = speed.get() * 0.5f;
            mc.player.setMotion(
                mc.player.getMotion().x * speedValue,
                mc.player.getMotion().y,
                mc.player.getMotion().z * speedValue
            );
        }
    }
}


