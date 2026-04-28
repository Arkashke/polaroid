package net.optifine.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.shapes.VoxelShape;
import polaroid.client.utils.render.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;

public class RenderUtils
{
    private static boolean flushRenderBuffers = true;
    private static Minecraft mc = Minecraft.getInstance();

    public static boolean setFlushRenderBuffers(boolean flushRenderBuffers)
    {
        boolean flag = RenderUtils.flushRenderBuffers;
        RenderUtils.flushRenderBuffers = flushRenderBuffers;
        return flag;
    }
    private static void lambda$drawBlockBoxWithProgress$1(BlockPos blockPos, int n, float f, double d, double d2, double d3, double d4, double d5, double d6) {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB((double)blockPos.getX() + d, (double)blockPos.getY() + d2, (double)blockPos.getZ() + d3, (double)blockPos.getX() + d4, (double)blockPos.getY() + d5, (double)blockPos.getZ() + d6).offset(-RenderUtils.mc.getRenderManager().info.getProjectedView().x, -RenderUtils.mc.getRenderManager().info.getProjectedView().y, -RenderUtils.mc.getRenderManager().info.getProjectedView().z);
        RenderUtils.drawBox(axisAlignedBB, n);
        AxisAlignedBB axisAlignedBB2 = new AxisAlignedBB((double)blockPos.getX() + d, (double)blockPos.getY() + d2, (double)blockPos.getZ() + d3, (double)blockPos.getX() + d4, (double)blockPos.getY() + d2 + (double)f * (d5 - d2), (double)blockPos.getZ() + d6).offset(-RenderUtils.mc.getRenderManager().info.getProjectedView().x, -RenderUtils.mc.getRenderManager().info.getProjectedView().y, -RenderUtils.mc.getRenderManager().info.getProjectedView().z);
        float f2 = 1.0f - f;
        int n2 = ColorUtils.rgba(ColorUtils.getColor(0), ColorUtils.getColor(0), ColorUtils.getColor(0), (int)(f2 * 255.0f));
        RenderUtils.drawBox(axisAlignedBB2, n2);
    }
    public static void drawBlockBoxWithProgress(BlockPos blockPos, int n, float f) {
        BlockState blockState = RenderUtils.mc.world.getBlockState(blockPos);
        VoxelShape voxelShape = blockState.getShape(RenderUtils.mc.world, blockPos);
        voxelShape.forEachBox((arg_0, arg_1, arg_2, arg_3, arg_4, arg_5) -> RenderUtils.lambda$drawBlockBoxWithProgress$1(blockPos, n, f, arg_0, arg_1, arg_2, arg_3, arg_4, arg_5));
    }
    public static boolean isFlushRenderBuffers()
    {
        return flushRenderBuffers;
    }

    public static void flushRenderBuffers()
    {
        if (flushRenderBuffers)
        {
            RenderTypeBuffers rendertypebuffers = mc.getRenderTypeBuffers();
            rendertypebuffers.getBufferSource().flushRenderBuffers();
            rendertypebuffers.getCrumblingBufferSource().flushRenderBuffers();
        }
    }

    public static void drawBlockBox(BlockPos blockPos, int color) {
        drawBox(new AxisAlignedBB(blockPos).offset(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z), color);
    }

    public static void drawBox(AxisAlignedBB bb, int color) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL_DEPTH_TEST);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glLineWidth(1);
        float[] rgb = ColorUtils.rgba(color);
        GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glPopMatrix();

    }
}


