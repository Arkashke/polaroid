package polaroid.client.modules.impl.movement;


import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import net.minecraft.entity.projectile.*;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.vector.Vector3d;

@ModuleSystem(name = "AutoDodge", type = Category.Movement, server = ServerCategory.NO, description = "Уворачивается от снарядов и негативных зелий")
public class AutoDodge extends Module {
    private static final double DETECTION_RADIUS = 8.0;
    private static final double EVASION_POWER = 0.8;
    private static final float JUMP_POWER = 0.4F;
    private long lastDodgeTime;

    public AutoDodge() {
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.world == null || mc.player == null || System.currentTimeMillis() - lastDodgeTime < 1000) return;

        for (Object entity : mc.world.getAllEntities()) {
            if (entity instanceof ProjectileEntity) {
                ProjectileEntity projectile = (ProjectileEntity) entity;

                if (shouldDodge(projectile) && isProjectileDangerous(projectile)) {
                    Vector3d predictedPos = predictImpactPosition(projectile);

                    if (mc.player.getDistanceSq(predictedPos) <= 16.0) {
                        performEvasion(projectile);
                        lastDodgeTime = System.currentTimeMillis();
                        break;
                    }
                }
            }
        }
    }

    private boolean shouldDodge(ProjectileEntity projectile) {
        return projectile.getDistance(mc.player) <= DETECTION_RADIUS &&
                !isProjectileHarmless(projectile);
    }

    private boolean isProjectileDangerous(ProjectileEntity projectile) {
        if (projectile instanceof PotionEntity) {
            return PotionUtils.getEffectsFromStack(((PotionEntity) projectile).getItem())
                    .stream()
                    .anyMatch(effect -> !effect.getPotion().isBeneficial());
        }
        return true;
    }

    private boolean isProjectileHarmless(ProjectileEntity projectile) {
        if (projectile instanceof AbstractArrowEntity) {
            return ((AbstractArrowEntity) projectile).isOnGround();
        }
        if (projectile instanceof PotionEntity) {
            return projectile.isOnGround();
        }
        return false;
    }

    private Vector3d predictImpactPosition(ProjectileEntity projectile) {
        Vector3d position = projectile.getPositionVec();
        Vector3d motion = projectile.getMotion();
        float gravity = getProjectileGravity(projectile);
        int steps = (int) (DETECTION_RADIUS / motion.length());

        for (int i = 0; i < steps && motion.length() > 0.1; i++) {
            motion = applyGravity(motion, gravity);
            position = position.add(motion);
        }
        return position;
    }

    private Vector3d applyGravity(Vector3d motion, float gravity) {
        return new Vector3d(
                motion.x * 0.99,
                motion.y - gravity,
                motion.z * 0.99
        );
    }

    private float getProjectileGravity(ProjectileEntity projectile) {
        if (projectile instanceof AbstractArrowEntity) {
            return 0.05F;
        } else if (projectile instanceof PotionEntity) {
            return 0.03F;
        } else if (projectile instanceof FireballEntity) {
            return 0.0F;
        }
        return 0.05F;
    }

    private void performEvasion(ProjectileEntity projectile) {
        Vector3d projectileDir = projectile.getMotion().normalize();
        Vector3d evasionDirection = new Vector3d(-projectileDir.z, JUMP_POWER, projectileDir.x).normalize();

        mc.player.setMotion(
                evasionDirection.x * EVASION_POWER,
                JUMP_POWER,
                evasionDirection.z * EVASION_POWER
        );
    }

    @Override
    public boolean onDisable() {
        super.onDisable();
        lastDodgeTime = 0;
        return false;
    }
}

