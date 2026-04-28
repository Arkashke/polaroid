//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package polaroid.client.utils.rotation.impl;

import polaroid.client.Polaroid;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.rotation.Rotation;
import polaroid.client.utils.rotation.RotationHandler;
import java.security.SecureRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class SpookyTimeRotation extends RotationHandler {
    private final StopWatch attackTimer = new StopWatch();
    private final StopWatch multipointResetTimer = new StopWatch();
    private int attackCount = 0;
    private final SecureRandom random = new SecureRandom();
    private long lastServerTick = 0L;
    private int ticksSinceLastRotation = 0;
    private static final int SERVER_TICK_MS = 50;
    private static final long MULTIPOINT_RESET_INTERVAL = 120000L;
    private int currentMultipointStrategy = 0;
    private double dynamicOffsetX = 0.0;
    private double dynamicOffsetZ = 0.0;
    private long lastOffsetUpdate = 0L;

    public SpookyTimeRotation() {
        super("SpookyTime");
        this.multipointResetTimer.reset();
    }

    public Vector3d randomValue() {
        float jitterSpeed = 4.5F;
        float jitterYaw = (float)((Math.random() - (double)0.5F) * (double)jitterSpeed);
        float jitterPitch = (float)((Math.random() - (double)0.5F) * (double)(jitterSpeed * 0.6F));
        return new Vector3d((double)jitterYaw, (double)jitterPitch, (double)0.0F);
    }

    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity target) {
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        if (target != null) {
            Minecraft var10000 = mc;
            if (Minecraft.player != null && target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity)target;
                if (this.multipointResetTimer.isReached(120000L)) {
                    this.currentMultipointStrategy = (this.currentMultipointStrategy + 1) % 3;
                    this.multipointResetTimer.reset();
                }

                long currentTime = System.currentTimeMillis();
                long timeSinceLastTick = currentTime - this.lastServerTick;
                if (timeSinceLastTick >= 50L) {
                    this.lastServerTick = currentTime;
                    ++this.ticksSinceLastRotation;
                }

                this.ticksSinceLastRotation = 0;
                double[] heightPoints;
                switch (this.currentMultipointStrategy) {
                    case 0 -> heightPoints = new double[]{0.7, 0.85, 0.9, 0.95};
                    case 1 -> heightPoints = new double[]{0.2, 0.45, 0.7, 0.9};
                    case 2 -> heightPoints = new double[]{0.15, 0.3, 0.45, 0.6};
                    default -> heightPoints = new double[]{0.2, 0.45, 0.7, 0.9};
                }

                var10000 = mc;
                double eyeY = Minecraft.player.getPosY() + 1.62;
                double bestHeight = heightPoints[1];
                double minAngleDiff = Double.MAX_VALUE;

                for(double heightPoint : heightPoints) {
                    double testY = livingTarget.getPosY() + (double)livingTarget.getHeight() * heightPoint;
                    double var10002 = livingTarget.getPosX();
                    Minecraft var10003 = mc;
                    var10002 -= Minecraft.player.getPosX();
                    double var66 = testY - eyeY;
                    double var10004 = livingTarget.getPosZ();
                    Minecraft var10005 = mc;
                    Vector3d testVec = new Vector3d(var10002, var66, var10004 - Minecraft.player.getPosZ());
                    float testYaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(testVec.z, testVec.x)) - (double)90.0F);
                    float testPitch = (float)(-Math.toDegrees(Math.atan2(testVec.y, Math.sqrt(testVec.x * testVec.x + testVec.z * testVec.z))));
                    float deltaY = MathHelper.wrapDegrees(testYaw - currentRotation.getYaw());
                    float deltaP = testPitch - currentRotation.getPitch();
                    double angleDiff = Math.sqrt((double)(deltaY * deltaY + deltaP * deltaP));
                    if (angleDiff < minAngleDiff) {
                        minAngleDiff = angleDiff;
                        bestHeight = heightPoint;
                    }
                }

                double bodyVariation = Math.sin((double)(currentTime / 50L) * 0.15) * 0.08;
                double targetHeight = bestHeight + bodyVariation;
                double targetY = livingTarget.getPosY() + (double)livingTarget.getHeight() * targetHeight;
                
                // Динамическое смещение внутри хитбокса
                if (currentTime - this.lastOffsetUpdate > 100L) {
                    double hitboxWidth = livingTarget.getBoundingBox().maxX - livingTarget.getBoundingBox().minX;
                    double hitboxDepth = livingTarget.getBoundingBox().maxZ - livingTarget.getBoundingBox().minZ;
                    
                    // Плавное изменение смещения с использованием синусоид разной частоты
                    double timeFactorX = (double)(currentTime / 80L) * 0.1;
                    double timeFactorZ = (double)(currentTime / 100L) * 0.1;
                    
                    this.dynamicOffsetX = Math.sin(timeFactorX) * hitboxWidth * 0.25;
                    this.dynamicOffsetZ = Math.cos(timeFactorZ) * hitboxDepth * 0.25;
                    
                    this.lastOffsetUpdate = currentTime;
                }
                
                Vector3d targetMotion = livingTarget.getMotion();
                double predictedX = livingTarget.getPosX() + targetMotion.x * (double)0.5F + this.dynamicOffsetX;
                double predictedZ = livingTarget.getPosZ() + targetMotion.z * (double)0.5F + this.dynamicOffsetZ;
                Minecraft var67 = mc;
                double var65 = predictedX - Minecraft.player.getPosX();
                double var68 = targetY - eyeY;
                Minecraft var69 = mc;
                Vector3d diffVec = new Vector3d(var65, var68, predictedZ - Minecraft.player.getPosZ());
                float tYaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diffVec.z, diffVec.x)) - (double)90.0F);
                float tPitch = (float)(-Math.toDegrees(Math.atan2(diffVec.y, Math.sqrt(diffVec.x * diffVec.x + diffVec.z * diffVec.z))));
                float deltaY = MathHelper.wrapDegrees(tYaw - currentRotation.getYaw());
                float deltaP = tPitch - currentRotation.getPitch();
                float dist = (float)Math.sqrt((double)(deltaY * deltaY + deltaP * deltaP));
                float acceleration = dist > 15.0F ? 0.85F : 0.65F;
                var10000 = mc;
                if (Minecraft.player.isSprinting()) {
                    acceleration += 0.12F;
                }

                float maxRotationSpeed = 95.0F;
                Vector3d jitter = this.randomValue();
                float jitterYaw = (float)jitter.x * 0.5F;
                float jitterPitch = (float)jitter.y * 0.5F;
                float stepY = deltaY * acceleration + jitterYaw;
                float stepP = deltaP * acceleration * 0.75F + jitterPitch;
                stepY = MathHelper.clamp(stepY, -maxRotationSpeed, maxRotationSpeed);
                stepP = MathHelper.clamp(stepP, -35.0F, 35.0F);
                float mouseSensitivity = (float)mc.gameSettings.mouseSensitivity;
                float fCalculation = mouseSensitivity * 0.6F + 0.2F;
                float mouseGcd = fCalculation * fCalculation * fCalculation * 1.2F;
                float finalRdyYaw = currentRotation.getYaw() + stepY;
                float finalRdyPitch = MathHelper.clamp(currentRotation.getPitch() + stepP, -89.0F, 89.0F);
                finalRdyYaw -= (finalRdyYaw - currentRotation.getYaw()) % mouseGcd;
                finalRdyPitch -= (finalRdyPitch - currentRotation.getPitch()) % mouseGcd;
                if (aura.getCorrectionType().is("Фокусированная")) {
                    var10000 = mc;
                    Minecraft.player.rotationYawOffset = finalRdyYaw;
                    var10000 = mc;
                    Minecraft.player.rotationYawHead = finalRdyYaw;
                    var10000 = mc;
                    Minecraft.player.renderYawOffset = finalRdyYaw;
                    var10000 = mc;
                    Minecraft.player.rotationPitchHead = finalRdyPitch;
                }

                return new Rotation(finalRdyYaw, finalRdyPitch);
            }
        }

        return currentRotation;
    }
}


