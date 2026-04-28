package polaroid.client.utils.rotation.impl;

import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.rotation.Rotation;
import polaroid.client.utils.rotation.RotationHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.security.SecureRandom;

public class FunTimeRotation extends RotationHandler {
    private final StopWatch timer = new StopWatch();

    public static int attackCount = 0;
    public static long lastAttack = 0L;

    public FunTimeRotation() {
        super("FunTime");
    }

    public static void updateAttackState(boolean attack) {
        if (attack) {
            ++attackCount;
            lastAttack = System.currentTimeMillis();
        }
    }

    @Override
    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation, Vector3d vec3d, Entity entity) {
        long attackTimer = System.currentTimeMillis() - lastAttack;
        int count = attackCount;

        Rotation rotationDelta = Rotation.calculateDelta(currentRotation, targetRotation);
        float yawDelta = rotationDelta.getYaw();
        float pitchDelta = rotationDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        if (entity != null) {
            boolean canAttack = willClickSoon();
            float speed = canAttack ? 1 : new SecureRandom().nextBoolean() ? 0.4F : 0.2F;
            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);
            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

            Rotation moveTurns = new Rotation(currentRotation.getYaw(), currentRotation.getPitch());
            moveTurns.setYaw(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentRotation.getYaw(), currentRotation.getYaw() + moveYaw));
            moveTurns.setPitch(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentRotation.getPitch(), currentRotation.getPitch() + movePitch));
            return moveTurns;
        } else {
            int suck = count % 3;
            float speed = attackTimer > 430 ? new SecureRandom().nextBoolean() ? 0.4F : 0.2F : -0.2F;
            float random = attackTimer / 40F + (count % 6);

            Rotation randomTurns = switch (suck) {
                case 0 -> new Rotation((float) Math.cos(random), (float) Math.sin(random));
                case 1 -> new Rotation((float) Math.sin(random), (float) Math.cos(random));
                case 2 -> new Rotation((float) Math.sin(random), (float) -Math.cos(random));
                default -> new Rotation((float) -Math.cos(random), (float) Math.sin(random));
            };

            float yaw = attackTimer < 2000 ? randomLerp(12, 24) * randomTurns.getYaw() : 0;
            float pitch2 = randomLerp(0, 2) * (float) Math.cos((double) System.currentTimeMillis() / 5000);
            float pitch = attackTimer < 2000 ? randomLerp(2, 6) * randomTurns.getPitch() + pitch2 : 0;

            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);
            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

            Rotation moveTurns = new Rotation(currentRotation.getYaw(), currentRotation.getPitch());
            moveTurns.setYaw(MathHelper.lerp(MathHelper.clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentRotation.getYaw(), currentRotation.getYaw() + moveYaw) + yaw);
            moveTurns.setPitch(MathHelper.lerp(MathHelper.clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentRotation.getPitch(), currentRotation.getPitch() + movePitch) + pitch);
            return moveTurns;
        }
    }

    @Override
    public Vector3d randomValue() {
        return new Vector3d(0, 0, 0);
    }

    protected float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    private boolean willClickSoon() {
        float attackStrength = mc.player.getCooledAttackStrength(0.5F);
        return attackStrength >= 0.92F;
    }
}


