package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;

import polaroid.client.events.JumpEvent;
import polaroid.client.events.WorldEvent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.render.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.concurrent.CopyOnWriteArrayList;

@ModuleSystem(name = "JumpCircle", type = Category.Render, server = ServerCategory.NO, description = "Создаёт круг под игроком")
public class JumpCircle extends Module {
    public static final ModeSetting mode = new ModeSetting("Текстура", "Glow","Glow","New");
    public JumpCircle(){
        addSettings(mode);
    }


    private final CopyOnWriteArrayList<Circle> circles = new CopyOnWriteArrayList<>();

    @Subscribe
    private void onJump(JumpEvent e) {
        circles.add(new Circle(mc.player.getPositon(mc.getRenderPartialTicks()).add(0,0.05, 0)));
    }

    private final ResourceLocation circle1 = new ResourceLocation("polaroid/images/modules/jumpCircle/circle2.png");

    private final ResourceLocation circle2 = new ResourceLocation("polaroid/images/modules/jumpCircle/circle.png");

    @Subscribe
    private void onRender(WorldEvent e) {

        double sin = Math.sin((double) System.currentTimeMillis() / 1000.0);
        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 1, 0, 1);
        GlStateManager.translated(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y,-mc.getRenderManager().info.getProjectedView().z);

        // render
        {
            for (Circle c : circles) {

                if (mode.is("Glow"))
                    mc.getTextureManager().bindTexture(circle1);
                if (mode.is("New"))
                    mc.getTextureManager().bindTexture(circle2);
                if (System.currentTimeMillis() - c.time > 1200) circles.remove(c);
                long lifeTime = System.currentTimeMillis() - c.time;

                c.animation.update();
                float rad = (float) c.animation.getValue()/0.7f ;

                Vector3d vector3d = c.vector3d;

                vector3d = vector3d.add(-rad / 2f, 0 ,-rad / 2f);

                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
                float fadeFactor = 1f - (lifeTime / 1200f);
                int alpha = (int) (255 * fadeFactor);
                buffer.pos(vector3d.x, vector3d.y, vector3d.z).color(ColorUtils.setAlpha(ColorUtils.getColor(5), alpha)).tex(0,0).endVertex();
                buffer.pos(vector3d.x + rad, vector3d.y, vector3d.z).color(ColorUtils.setAlpha(ColorUtils.getColor(10), alpha)).tex(1,0).endVertex();
                buffer.pos(vector3d.x + rad, vector3d.y, vector3d.z + rad).color(ColorUtils.setAlpha(ColorUtils.getColor(15), alpha)).tex(1,1).endVertex();
                buffer.pos(vector3d.x, vector3d.y, vector3d.z + rad).color(ColorUtils.setAlpha(ColorUtils.getColor(20), alpha)).tex(0,1).endVertex();
                buffer.pos(vector3d.x, vector3d.y, vector3d.z).color(ColorUtils.setAlpha(ColorUtils.getColor(5), alpha)).tex(0,0).endVertex();
                buffer.pos(vector3d.x + rad, vector3d.y, vector3d.z).color(ColorUtils.setAlpha(ColorUtils.getColor(10), alpha)).tex(1,0).endVertex();
                buffer.pos(vector3d.x + rad, vector3d.y, vector3d.z + rad).color(ColorUtils.setAlpha(ColorUtils.getColor(15), alpha)).tex(1,1).endVertex();
                buffer.pos(vector3d.x, vector3d.y, vector3d.z + rad).color(ColorUtils.setAlpha(ColorUtils.getColor(20), alpha)).tex(0,1).endVertex();



                tessellator.draw();
            }

        }

        GlStateManager.disableBlend();
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableAlphaTest();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }


    private class Circle {

        private final Vector3d vector3d;

        private final long time;
        private final Animation animation = new Animation();
        private boolean isBack;

        public Circle(Vector3d vector3d) {
            this.vector3d = vector3d;
            time = System.currentTimeMillis();
            animation.animate(1, 4, Easings.ELASTIC_OUT);
        }

    }

}

