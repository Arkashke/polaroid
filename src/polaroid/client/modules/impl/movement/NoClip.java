package polaroid.client.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventPacket;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import net.minecraft.block.BlockState;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleSystem(name = "NoClip", type = Category.Movement, server = ServerCategory.NO, description = "Позволяет проходить сквозь блоки")
public class NoClip extends Module {

    private final List<IPacket<?>> packets = new CopyOnWriteArrayList<>();
    private AxisAlignedBB lastBox;

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.noClip) {
            mc.player.setMotion(0, 0, 0);
            
            float speed = 0.2f;
            
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.setMotion(mc.player.getMotion().x, speed, mc.player.getMotion().z);
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.setMotion(mc.player.getMotion().x, -speed, mc.player.getMotion().z);
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (mc.player == null || mc.world == null) return;

        if (event.isSend()) {
            IPacket<?> packet = event.getPacket();
            
            if (shouldPhase() && !(packet instanceof CKeepAlivePacket)) {
                if (packet instanceof CPlayerPacket) {
                    packets.add(packet);
                    event.cancel();
                }
            }
        }

        if (event.isReceive() && event.getPacket() instanceof SPlayerPositionLookPacket) {
            resumePackets();
            
            double x = mc.player.getPosX();
            double y = mc.player.getPosY();
            double z = mc.player.getPosZ();
            float yaw = mc.player.rotationYaw;
            float pitch = mc.player.rotationPitch;
            
            mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(
                x - 1000, y, z - 1000, yaw, pitch, false
            ));
            
            mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(
                x, y, z, yaw, pitch, mc.player.isOnGround()
            ));
        }
    }

    private boolean shouldPhase() {
        if (mc.player == null || mc.world == null) return false;
        
        AxisAlignedBB hitbox = mc.player.getBoundingBox();
        BlockPos min = new BlockPos(
            (int) Math.floor(hitbox.minX),
            (int) Math.floor(hitbox.minY),
            (int) Math.floor(hitbox.minZ)
        );
        BlockPos max = new BlockPos(
            (int) Math.floor(hitbox.maxX),
            (int) Math.floor(hitbox.maxY),
            (int) Math.floor(hitbox.maxZ)
        );

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    
                    if (!state.isAir() && !state.getCollisionShape(mc.world, pos).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void resumePackets() {
        if (mc.player == null || mc.world == null) return;
        
        if (!packets.isEmpty()) {
            for (IPacket<?> packet : new ArrayList<>(packets)) {
                mc.player.connection.sendPacket(packet);
            }
            packets.clear();
            lastBox = mc.player.getBoundingBox();
        }
    }

    @Override
    public boolean onEnable() {
        if (mc.player != null) {
            mc.player.noClip = true;
        }
        super.onEnable();
        return false;
    }

    @Override
    public boolean onDisable() {
        if (mc.player != null) {
            mc.player.noClip = false;
        }
        resumePackets();
        lastBox = null;
        super.onDisable();
        return false;
    }
}


