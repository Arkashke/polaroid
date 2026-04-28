package polaroid.client.utils.rotation;

import polaroid.client.utils.client.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class RotationMath implements IMinecraft {

    public static Rotation calculateAngle(Vector3d target) {
        Vector3d eyePos = mc.player.getEyePosition(1.0f);
        
        double deltaX = target.x - eyePos.x;
        double deltaY = target.y - eyePos.y;
        double deltaZ = target.z - eyePos.z;
        
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDistance));
        
        return new Rotation(yaw, MathHelper.clamp(pitch, -90.0f, 90.0f));
    }

    public static Vector3d getTargetPoint(Entity entity, float minY, float maxY) {
        Vector3d entityPos = entity.getPositionVec();
        double height = entity.getHeight();
        
        float randomY = minY + (float) Math.random() * (maxY - minY);
        double targetY = entityPos.y + height * randomY;
        
        return new Vector3d(entityPos.x, targetY, entityPos.z);
    }
}


