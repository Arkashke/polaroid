package polaroid.client.utils.math;

import polaroid.client.utils.client.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

public class PlayerPositionTracker implements IMinecraft {
    private final static IntBuffer viewport;
    private final static FloatBuffer modelview;
    private final static FloatBuffer projection;
    private final static FloatBuffer vector;

    static {
        viewport = BufferUtils.createIntBuffer(16);
        modelview = BufferUtils.createFloatBuffer(16);
        projection = BufferUtils.createFloatBuffer(16);
        vector = BufferUtils.createFloatBuffer(4);
    }

    public static class Vector4d {
        public double x, y, z, w;
        
        public Vector4d(double x, double y, double z, double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }

    public static boolean isInView(Entity ent) {
        if (mc.getRenderViewEntity() == null) return false;
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, 
            mc.getRenderManager().info.getProjectedView().y,
            mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }

    public static Vector4d updatePlayerPositions(Entity player, float partialTicks) {
        Vector3d projection = mc.getRenderManager().info.getProjectedView();
        double x = MathUtil.interpolate(player.getPosX(), player.lastTickPosX, partialTicks);
        double y = MathUtil.interpolate(player.getPosY(), player.lastTickPosY, partialTicks);
        double z = MathUtil.interpolate(player.getPosZ(), player.lastTickPosZ, partialTicks);

        Vector3d size = new Vector3d(
                player.getBoundingBox().maxX - player.getBoundingBox().minX,
                player.getBoundingBox().maxY - player.getBoundingBox().minY,
                player.getBoundingBox().maxZ - player.getBoundingBox().minZ
        );

        AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2f, y, z - size.z / 2f, x + size.x / 2f, y + size.y, z + size.z / 2f);

        Vector4d position = null;

        for (int i = 0; i < 8; i++) {
            Vector3d vector = new Vector3d(
                    i % 2 == 0 ? aabb.minX : aabb.maxX,
                    (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY,
                    (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

            vector = project2D(vector.x - projection.x, vector.y - projection.y, vector.z - projection.z);

            if (vector != null && vector.z >= 0.0 && vector.z < 1.0) {
                if (position == null) {
                    position = new Vector4d(vector.x, vector.y, vector.z, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }
        }

        return position;
    }

    private static Vector3d project2D(final double x, final double y, final double z) {
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelview);
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        
        if (gluProject((float) x, (float) y, (float) z, modelview, projection, viewport, vector)) {
            float screenX = vector.get(0);
            float screenY = vector.get(1);
            float screenZ = vector.get(2);
            
            return new Vector3d(screenX / 2, (mc.getMainWindow().getHeight() - screenY) / 2, screenZ);
        }
        return null;
    }

    private static boolean gluProject(float objX, float objY, float objZ, FloatBuffer modelMatrix, 
                                     FloatBuffer projMatrix, IntBuffer viewport, FloatBuffer winPos) {
        float[] in = new float[4];
        float[] out = new float[4];

        in[0] = objX;
        in[1] = objY;
        in[2] = objZ;
        in[3] = 1.0f;

        transformVec4(out, in, modelMatrix);
        transformVec4(in, out, projMatrix);

        if (in[3] == 0.0f) return false;

        float w = 1.0f / in[3];
        in[0] *= w;
        in[1] *= w;
        in[2] *= w;

        winPos.put(0, viewport.get(0) + (1.0f + in[0]) * viewport.get(2) / 2.0f);
        winPos.put(1, viewport.get(1) + (1.0f + in[1]) * viewport.get(3) / 2.0f);
        winPos.put(2, (1.0f + in[2]) / 2.0f);

        return true;
    }

    private static void transformVec4(float[] out, float[] in, FloatBuffer matrix) {
        for (int i = 0; i < 4; i++) {
            out[i] = in[0] * matrix.get(i) + in[1] * matrix.get(4 + i) + 
                     in[2] * matrix.get(8 + i) + in[3] * matrix.get(12 + i);
        }
    }
}


