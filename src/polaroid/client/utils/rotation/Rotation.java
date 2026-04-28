package polaroid.client.utils.rotation;

import polaroid.client.utils.client.IMinecraft;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;

@Getter
@Setter
@AllArgsConstructor
public class Rotation implements IMinecraft {
    private float yaw;
    private float pitch;

    public Rotation() {
        this(0F, 0F);
    }

    public Rotation adjustSensitivity() {
        float f = (float) (mc.gameSettings.mouseSensitivity * 0.6F + 0.2F);
        float gcd = f * f * f * 1.2F;
        
        float deltaYaw = yaw - mc.player.rotationYaw;
        float deltaPitch = pitch - mc.player.rotationPitch;
        
        deltaYaw -= deltaYaw % gcd;
        deltaPitch -= deltaPitch % gcd;
        
        return new Rotation(mc.player.rotationYaw + deltaYaw, mc.player.rotationPitch + deltaPitch);
    }

    public static Rotation calculateDelta(Rotation current, Rotation target) {
        float yawDelta = MathHelper.wrapDegrees(target.yaw - current.yaw);
        float pitchDelta = target.pitch - current.pitch;
        return new Rotation(yawDelta, pitchDelta);
    }
}


