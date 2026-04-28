//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.events.EventUpdate;
import polaroid.client.events.WorldEvent;
import polaroid.client.events.EventDisplay.Type;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.impl.combat.Aura;
import polaroid.client.modules.settings.Setting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.utils.animations.Animation;
import polaroid.client.utils.animations.Direction;
import polaroid.client.utils.animations.impl.DecelerateAnimation;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.projections.ProjectionUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

@ModuleSystem(
        name = "TargetESP",
        type = Category.Render,
        server = ServerCategory.NO,
        description = "Отображает кого таргетит Aura"
)
public class TargetESP extends Module {
    private final ModeSetting type = new ModeSetting("Тип", "Кольцо", new String[]{"Кольцо", "Ромб", "Квадрат", "Призраки", "Кристаллы"});
    private final Animation alpha = new DecelerateAnimation(600, (double) 255.0F);
    private LivingEntity currentTarget;
    public static LivingEntity target = null;
    private final Aura aura;
    private double speed;
    private long lastTime = System.currentTimeMillis();
    public static long startTime = System.currentTimeMillis();

    public TargetESP(Aura aura) {
        this.aura = aura;
        this.addSettings(new Setting[]{this.type});
    }

    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);
        return Math.max((double) 10.0F, (double) 1000.0F / distance) * (size / (double) 30.0F) / (fov == (double) 70.0F ? (double) 1.0F : fov / (double) 70.0F);
    }

    @Subscribe
    private void onUpdate(EventUpdate eventUpdate) {
        Aura aura = Polaroid.getInstance().getFunctionRegistry().getAura();
        if (aura.getTarget() != null) {
            this.currentTarget = aura.getTarget();
        }

        this.alpha.setDirection(aura.isState() && aura.getTarget() != null ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Subscribe
    private void onWorldEvent(WorldEvent e) {
        if (type.is("ВекСайд")) {
            EntityRendererManager rm = mc.getRenderManager();
            if (aura.getTarget() == null) return;

            double x = aura.getTarget().lastTickPosX + (aura.getTarget().getPosX() - aura.getTarget().lastTickPosX) * (double) e.getPartialTicks() - rm.info.getProjectedView().getX();
            double y = aura.getTarget().lastTickPosY + (aura.getTarget().getPosY() - aura.getTarget().lastTickPosY) * (double) e.getPartialTicks() - rm.info.getProjectedView().getY();
            double z = aura.getTarget().lastTickPosZ + (aura.getTarget().getPosZ() - aura.getTarget().lastTickPosZ) * (double) e.getPartialTicks() - rm.info.getProjectedView().getZ();
            float height = aura.getTarget().getHeight();

            double duration = 1500;
            double elapsed = (System.currentTimeMillis() % duration);

            boolean side = elapsed > (duration / 2);
            double progress = elapsed / (duration / 2);

            if (side) progress -= 1;
            else progress = 1 - progress;

            progress = (progress < 0.5) ? 2 * progress * progress : 1 - Math.pow((-2 * progress + 2), 2) / 2;

            double eased = (height / 3) * ((progress > 0.5) ? 1 - progress : progress) * ((side) ? -1 : 1);

            RenderSystem.pushMatrix();
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            RenderSystem.disableCull();

            RenderSystem.lineWidth(5);
            RenderSystem.color4f(-1f, -1f, -1f, -1f);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);

            for (int i = 0; i <= 360; i++) {
                buffer.pos(x + Math.cos(Math.toRadians(i)) * aura.getTarget().getWidth() * 1, y + (height * progress), z + Math.sin(Math.toRadians(i)) * aura.getTarget().getWidth() * 1)
                        .color(ColorUtils.setAlpha(Theme.getColor(0), 175)).endVertex();
                buffer.pos(x + Math.cos(Math.toRadians(i)) * aura.getTarget().getWidth() * 1, y + (height * progress) + eased, z + Math.sin(Math.toRadians(i)) * aura.getTarget().getWidth() * 1)
                        .color(ColorUtils.setAlpha(Theme.getColor(0), 1)).endVertex();
            }

            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);
            RenderSystem.color4f(-5f, -5f, -5f, -5f);

            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

            for (int i = 0; i <= 360; i++) {
                buffer.pos(x + Math.cos(Math.toRadians(i)) * aura.getTarget().getWidth() * 1, y + (height * progress), z + Math.sin(Math.toRadians(i)) * aura.getTarget().getWidth() * 1)
                        .color(ColorUtils.setAlpha(Theme.getColor(0), 1)).endVertex();
            }

            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            RenderSystem.enableAlphaTest();
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            RenderSystem.shadeModel(GL11.GL_FLAT);
            RenderSystem.popMatrix();

        }
        if (this.type.is("Кольцо")) {
            EntityRendererManager rm = mc.getRenderManager();
            if (this.aura.isState() && this.aura.getTarget() != null) {
                double x = this.aura.getTarget().lastTickPosX + (this.aura.getTarget().getPosX() - this.aura.getTarget().lastTickPosX) * (double) e.getPartialTicks() - rm.info.getProjectedView().getX();
                double y = this.aura.getTarget().lastTickPosY + (this.aura.getTarget().getPosY() - this.aura.getTarget().lastTickPosY) * (double) e.getPartialTicks() - rm.info.getProjectedView().getY();
                double z = this.aura.getTarget().lastTickPosZ + (this.aura.getTarget().getPosZ() - this.aura.getTarget().lastTickPosZ) * (double) e.getPartialTicks() - rm.info.getProjectedView().getZ();
                float height = this.aura.getTarget().getHeight();
                double radius = (double) 2000.0F;
                double elapsed = (double) System.currentTimeMillis() % radius;
                boolean side = elapsed > radius / (double) 2.0F;
                double progress = elapsed / (radius / (double) 2.0F);
                progress = side ? --progress : (double) 1.0F - progress;
                progress = progress < (double) 0.5F ? (double) 2.0F * progress * progress : (double) 1.0F - Math.pow((double) -2.0F * progress + (double) 2.0F, (double) 2.0F) / (double) 2.0F;
                double eased = (double) (height / 2.0F) * (progress > (double) 0.5F ? (double) 1.0F - progress : progress) * (double) (side ? -1 : 1);
                RenderSystem.pushMatrix();
                GL11.glDepthMask(false);
                GL11.glEnable(2848);
                GL11.glHint(3154, 4354);
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.disableAlphaTest();
                RenderSystem.shadeModel(7425);
                RenderSystem.disableCull();
                RenderSystem.lineWidth(4);
                float glowAlpha = 15.0F;
                float coreAlpha = 15.1F;
                RenderSystem.color4f(12, 12, 12, 12);
                
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                
                buffer.begin(8, DefaultVertexFormats.POSITION_COLOR);
                float[] colors = null;
                mc.getTextureManager().bindTexture(new ResourceLocation("polaroid/images/modules/targetEsp/glow.png"));
                for (int i = 0; i <= 360; ++i) {
                    colors = ColorUtils.IntColor.rgb(Theme.getColor(0));
                    buffer.pos(x + Math.cos(Math.toRadians((double) i)) * (double) this.aura.getTarget().getWidth() * 0.85, y + (double) height * progress, z + Math.sin(Math.toRadians((double) i)) * (double) this.aura.getTarget().getWidth() * 0.85).color(colors[0], colors[1], colors[2], 155.0F).endVertex();
                    buffer.pos(x + Math.cos(Math.toRadians((double) i)) * (double) this.aura.getTarget().getWidth() * 0.85, y + (double) height * progress + eased * 1.8, z + Math.sin(Math.toRadians((double) i)) * (double) this.aura.getTarget().getWidth() * 0.85).color(colors[0], colors[1], colors[2], 255).endVertex();
                }

                buffer.finishDrawing();
                WorldVertexBufferUploader.draw(buffer);
                RenderSystem.color4f(0.5F, 0.5F, 0.5F, 0.0F);
                buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);

                for (int var119 = 0; var119 <= 360; ++var119) {
                    buffer.pos(x + Math.cos(Math.toRadians((double) var119)) * (double) this.aura.getTarget().getWidth() * 0.85, y + (double) height * progress, z + Math.sin(Math.toRadians((double) var119)) * (double) this.aura.getTarget().getWidth() * 0.85).color(colors[0], colors[1], colors[2], coreAlpha).endVertex();
                }

                buffer.finishDrawing();
                WorldVertexBufferUploader.draw(buffer);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableAlphaTest();
                GL11.glDepthMask(true);
                GL11.glDisable(2848);
                GL11.glHint(3154, 4354);
                RenderSystem.shadeModel(7424);
                RenderSystem.popMatrix();
            }
        }

        // Ghosts mode - улучшенная версия
        if (this.type.is("Призраки") && this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = new MatrixStack();
            ms.push();
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            GL11.glDepthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);

            double radius = 0.55;
            float speed = 50;
            float size = 0.35f;
            double distance = 15;
            int length = 25;
            int maxAlpha = (int) (255 * this.alpha.getOutput() / 255.0);
            int alphaFactor = 12;
            ActiveRenderInfo camera = mc.getRenderManager().info;

            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(),
                    -mc.getRenderManager().info.getProjectedView().getY(),
                    -mc.getRenderManager().info.getProjectedView().getZ());

            Vector3d interpolated = MathUtil.interpolate(this.currentTarget.getPositionVec(), 
                    new Vector3d(this.currentTarget.lastTickPosX, this.currentTarget.lastTickPosY, this.currentTarget.lastTickPosZ), 
                    e.getPartialTicks());
            interpolated = interpolated.add(0, 0.8f, 0);
            ms.translate(interpolated.x, interpolated.y + 0.5f, interpolated.z);

            ResourceLocation glowTexture = new ResourceLocation("polaroid/images/modules/targetEsp/glow.png");
            mc.getTextureManager().bindTexture(glowTexture);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // Первая спираль
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(s, (c), -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                int color = Theme.getColor(0);
                int alphaVal = net.minecraft.util.math.MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                int finalColor = ColorUtils.setAlpha(color, alphaVal);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate(-(s), -(c), (c));
            }

            // Вторая спираль
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-s, s, -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                int color = Theme.getColor(90);
                int alphaVal = net.minecraft.util.math.MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                int finalColor = ColorUtils.setAlpha(color, alphaVal);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), -(s), (c));
            }

            // Третья спираль
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-(s), -(s), (c));
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                int color = Theme.getColor(180);
                int alphaVal = net.minecraft.util.math.MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                int finalColor = ColorUtils.setAlpha(color, alphaVal);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), (s), -(c));
            }
            
            // Четвертая спираль для более плотного эффекта
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate((s), -(c), (s));
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                int color = Theme.getColor(270);
                int alphaVal = net.minecraft.util.math.MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                int finalColor = ColorUtils.setAlpha(color, alphaVal);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.IntColor.rgb(finalColor)[0], ColorUtils.IntColor.rgb(finalColor)[1], ColorUtils.IntColor.rgb(finalColor)[2], ColorUtils.IntColor.rgb(finalColor)[3]).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate(-(s), (c), -(s));
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            GL11.glDepthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }

        // Рендер кристаллов
        renderCrystals(e);
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        if (e.getType() == Type.PRE) {
            if (this.currentTarget != null && !this.alpha.finished(Direction.BACKWARDS) && this.type.is("Ромб")) {
                double sin = Math.sin((double) System.currentTimeMillis() / (double) 800);
                Vector3d interpolated = this.currentTarget.getPositon(e.getPartialTicks());
                float animationProgress = (float) (alpha.getOutput() / 255.0f);
                float scale = 1.0f + (1.0f - animationProgress) * 0.8f;
                float size = scale * 120;
                Vector2f pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.currentTarget.getHeight() / 1.8F), interpolated.z);
                GlStateManager.pushMatrix();
                GlStateManager.translatef(pos.x, pos.y, 0.0F);
                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translatef(-pos.x, -pos.y, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 1);
                int alpha = (int) this.alpha.getOutput();
                DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/targetEsp/marker.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.setAlpha(Theme.getColor(0, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(90, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(180, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(270, 1.0F), alpha)));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    @Subscribe
    private void onDisplay1(EventDisplay e) {
        if (e.getType() == Type.PRE) {
            if (this.currentTarget != null && !this.alpha.finished(Direction.BACKWARDS) && this.type.is("Квадрат")) {
                double sin = Math.sin((double) System.currentTimeMillis() / (double) 700.0F);
                Vector3d interpolated = this.currentTarget.getPositon(e.getPartialTicks());
                float animationProgress = (float) (alpha.getOutput() / 255.0f);
                float scale = 1.0f + (1.0f - animationProgress) * 0.8f;
                float size = scale * 100;
                Vector2f pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.currentTarget.getHeight() / 1.8F), interpolated.z);
                GlStateManager.pushMatrix();
                GlStateManager.translatef(pos.x, pos.y, 0.0F);
                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translatef(-pos.x, -pos.y, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 1);
                int alpha = (int) this.alpha.getOutput();
                DisplayUtils.drawImageAlpha(new ResourceLocation("polaroid/images/modules/targetEsp/target1.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.setAlpha(Theme.getColor(0, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(90, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(180, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(270, 1.0F), alpha)));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    @Subscribe
    private void onDisplay2(EventDisplay e) {
        if (e.getType() == Type.PRE) {
            if (this.currentTarget != null && !this.alpha.finished(Direction.BACKWARDS) && this.type.is("Старый")) {
                double sin = Math.sin((double) System.currentTimeMillis() / (double) 1000.0F);
                Vector3d interpolated = this.currentTarget.getPositon(e.getPartialTicks());
                float size = (float) this.getScale(interpolated, (double) 9.0F);
                Vector2f pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.currentTarget.getHeight() / 1.8F), interpolated.z);
                GlStateManager.pushMatrix();
                GlStateManager.translatef(pos.x, pos.y, 0.0F);
                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translatef(-pos.x, -pos.y, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 1);
                int alpha = (int) this.alpha.getOutput();
                DisplayUtils.drawImage(new ResourceLocation("polaroid/images/modules/targetEsp/marker.png"), pos.x - 105 / 2.0F, pos.y - 105 / 2.0F, 90, 90, new Vector4i(ColorUtils.setAlpha(Theme.getColor(0, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(90, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(180, 1.0F), alpha), ColorUtils.setAlpha(Theme.getColor(270, 1.0F), alpha)));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    // Дополнительный метод для рендера кристаллов
    private void renderCrystals(WorldEvent e) {
        if (!this.type.is("Кристаллы") || this.currentTarget == null || this.currentTarget == mc.player) return;
        
        EntityRendererManager rm = mc.getRenderManager();
        double x = this.currentTarget.lastTickPosX + (this.currentTarget.getPosX() - this.currentTarget.lastTickPosX) * (double) e.getPartialTicks() - rm.info.getProjectedView().getX();
        double y = this.currentTarget.lastTickPosY + (this.currentTarget.getPosY() - this.currentTarget.lastTickPosY) * (double) e.getPartialTicks() - rm.info.getProjectedView().getY();
        double z = this.currentTarget.lastTickPosZ + (this.currentTarget.getPosZ() - this.currentTarget.lastTickPosZ) * (double) e.getPartialTicks() - rm.info.getProjectedView().getZ();
        float width = this.currentTarget.getWidth() * 0.9F; // Уменьшен радиус
        float height = this.currentTarget.getHeight();
        
        RenderSystem.pushMatrix();
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        // 3 линии кристаллов на разных высотах
        int crystalCount = 12; // Количество кристаллов в каждой линии
        float[] heightLevels = {0.25F, 0.5F, 0.75F}; // 3 уровня высоты (низ, середина, верх)
        
        for (float heightLevel : heightLevels) {
            for (int i = 0; i < crystalCount; i++) {
                float animationProgress = (float) this.alpha.getOutput() / 255.0F;
                double angleOffset = (System.currentTimeMillis() % 3600) * 0.1;
                double angle = Math.toRadians((360.0 / crystalCount) * i + angleOffset);
                
                float radius = width * (0.6F + 0.15F * animationProgress); // Уменьшен радиус
                double crystalX = x + Math.cos(angle) * radius;
                double crystalZ = z + Math.sin(angle) * radius;
                
                // Анимация высоты кристалла
                double heightOffset = Math.sin((System.currentTimeMillis() + i * 200) * 0.003) * 0.08;
                double crystalY = y + height * heightLevel + heightOffset;
                
                // Размер кристалла (уменьшен)
                float crystalHeight = 0.25F * animationProgress;
                float crystalBase = 0.05F * animationProgress;
                
                // Цвет кристалла
                int colorIndex = (int) ((360.0 / crystalCount) * i);
                float[] colors = ColorUtils.IntColor.rgb(Theme.getColor(colorIndex));
                int alphaVal = (int) (180 * animationProgress);
                
                // Рисуем кристалл (пирамиду)
                buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
                
                // Верхняя точка кристалла
                double tipX = crystalX;
                double tipY = crystalY + crystalHeight;
                double tipZ = crystalZ;
                
                // Основание кристалла (4 точки)
                double base1X = crystalX + crystalBase;
                double base1Z = crystalZ;
                double base2X = crystalX;
                double base2Z = crystalZ + crystalBase;
                double base3X = crystalX - crystalBase;
                double base3Z = crystalZ;
                double base4X = crystalX;
                double base4Z = crystalZ - crystalBase;
                
                // 4 грани пирамиды
                // Грань 1
                buffer.pos(tipX, tipY, tipZ).color(colors[0], colors[1], colors[2], alphaVal).endVertex();
                buffer.pos(base1X, crystalY, base1Z).color(colors[0], colors[1], colors[2], alphaVal / 2).endVertex();
                buffer.pos(base2X, crystalY, base2Z).color(colors[0], colors[1], colors[2], alphaVal / 2).endVertex();
                
                // Грань 2
                buffer.pos(tipX, tipY, tipZ).color(colors[0], colors[1], colors[2], alphaVal).endVertex();
                buffer.pos(base2X, crystalY, base2Z).color(colors[0], colors[1], colors[2], alphaVal / 2).endVertex();
                buffer.pos(base3X, crystalY, base3Z).color(colors[0], colors[1], colors[2], alphaVal / 2).endVertex();
                
                // Грань 3
                buffer.pos(tipX, tipY, tipZ).color(colors[0], colors[1], colors[2], alphaVal).endVertex();
                buffer.pos(base3X, crystalY, base3Z).color(colors[0], colors[1], colors[2], alphaVal / 2).endVertex();
                buffer.pos(base4X, crystalY, base4Z).color(colors[0], colors[1], colors[2], alphaVal / 2).endVertex();
                
                // Грань 4
                buffer.pos(tipX, tipY, tipZ).color(colors[0], colors[1], colors[2], alphaVal).endVertex();
                buffer.pos(base4X, crystalY, base4Z).color(colors[0], colors[1], colors[2], alphaVal / 2).endVertex();
                buffer.pos(base1X, crystalY, base1Z).color(colors[0], colors[1], colors[2], alphaVal / 2).endVertex();
                
                // Основание (2 треугольника для квадрата)
                buffer.pos(base1X, crystalY, base1Z).color(colors[0], colors[1], colors[2], alphaVal / 3).endVertex();
                buffer.pos(base2X, crystalY, base2Z).color(colors[0], colors[1], colors[2], alphaVal / 3).endVertex();
                buffer.pos(base3X, crystalY, base3Z).color(colors[0], colors[1], colors[2], alphaVal / 3).endVertex();
                
                buffer.pos(base1X, crystalY, base1Z).color(colors[0], colors[1], colors[2], alphaVal / 3).endVertex();
                buffer.pos(base3X, crystalY, base3Z).color(colors[0], colors[1], colors[2], alphaVal / 3).endVertex();
                buffer.pos(base4X, crystalY, base4Z).color(colors[0], colors[1], colors[2], alphaVal / 3).endVertex();
                
                buffer.finishDrawing();
                WorldVertexBufferUploader.draw(buffer);
            }
        }
        
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        RenderSystem.popMatrix();
    }
}


