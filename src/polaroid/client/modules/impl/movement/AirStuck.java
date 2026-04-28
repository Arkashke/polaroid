package polaroid.client.modules.impl.movement;

import polaroid.client.events.EventLivingUpdate;
import polaroid.client.events.EventMotion;
import polaroid.client.events.EventPacket;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;

@ModuleSystem(name = "AirStuck", type = Category.Movement, server = ServerCategory.NO, description = "Позволяет зависнуть в воздухе")
public class AirStuck extends Module {
    private boolean oldIsFlying;
    float yaw;
    float pitch;
    float yawoff;

    @Subscribe
    public void onMotion(EventMotion event) {
        if (mc.player != null && mc.player.ticksExisted % 10 == 0) {
            ClientPlayNetHandler connection = mc.player.connection;
            connection.sendPacket(new CPlayerPacket(mc.player.isOnGround()));
        }

        if (mc.player != null) {
            event.cancel();
        }

        if (mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }

        mc.player.rotationYawHead = this.yaw;
        mc.player.renderYawOffset = this.yawoff;
        mc.player.rotationPitchHead = this.pitch;
    }

    @Subscribe
    public void onLivingUpdate(EventLivingUpdate event) {
        if (mc.player != null) {
            mc.player.noClip = true;
            mc.player.setOnGround(false);
            mc.player.setMotion(0.0F, 0.0F, 0.0F);
            mc.player.abilities.isFlying = true;
        }
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (mc.player != null) {
            IPacket packet = event.getPacket();
            if (packet instanceof CPlayerPacket) {
                CPlayerPacket playerPacket = (CPlayerPacket) packet;
                if (playerPacket.moving) {
                    playerPacket.x = mc.player.getPosX();
                    playerPacket.y = mc.player.getPosY();
                    playerPacket.z = mc.player.getPosZ();
                }
                playerPacket.onGround = mc.player.isOnGround();
                if (playerPacket.rotating) {
                    playerPacket.yaw = mc.player.rotationYaw;
                    playerPacket.pitch = mc.player.rotationPitch;
                }
            }
        }
    }

    public boolean onEnable() {
        super.onEnable();
        if (mc.player != null) {
            this.oldIsFlying = mc.player.abilities.isFlying;
            ClientPlayerEntity player = mc.player;
            player.movementInput = new MovementInput();
            mc.player.moveForward = 0.0F;
            mc.player.moveStrafing = 0.0F;
            this.yaw = mc.player.rotationYaw;
            this.pitch = mc.player.rotationPitch;
            this.yawoff = mc.player.renderYawOffset;
        }
        return false;
    }

    public boolean onDisable() {
        super.onDisable();
        if (mc.player != null) {
            ClientPlayerEntity player = mc.player;
            player.movementInput = new MovementInputFromOptions(mc.gameSettings);
            mc.player.abilities.isFlying = this.oldIsFlying;
        }
        return false;
    }
}

