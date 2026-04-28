package polaroid.client.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.client.CPlayerPacket;
import polaroid.client.events.EventPacket;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;

@ModuleSystem(name = "NoServerRotate", type = Category.Player, server = ServerCategory.NO, description = "Убирает возможность сервера ставить камеру на любую точку")
public class NoRotate extends Module {
    private float targetYaw;
    private float targetPitch;
    private boolean isPacketSent;

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.isSend()) {
            if (this.isPacketSent) {
                if (event.getPacket() instanceof CPlayerPacket playerPacket) {
                    playerPacket.setRotation(targetYaw, targetPitch);
                    this.isPacketSent = false;
                }
            }
        }
    }

    public void sendRotationPacket(float yaw, float pitch) {
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.isPacketSent = true;
    }
}


