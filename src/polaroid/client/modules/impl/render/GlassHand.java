package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.utils.CustomFramebuffer;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.KawaseBlur;
import polaroid.client.utils.shader.impl.Outline;
import net.minecraft.client.settings.PointOfView;
import org.lwjgl.opengl.GL11;

@ModuleSystem(name = "Hands", type = Category.Render, server = ServerCategory.NO, description = "Изменяет руку игрока")
public class GlassHand extends Module {

    public CustomFramebuffer hands = new CustomFramebuffer(false).setLinear();
    public CustomFramebuffer mask = new CustomFramebuffer(false).setLinear();

    @Subscribe
    public void onRender(EventDisplay e) {
        if (e.getType() != EventDisplay.Type.HIGH) {
            return;
        }

        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            KawaseBlur.blur.updateBlur(3, 4);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlphaTest();
            ColorUtils.setColor(ColorUtils.getColor(0));
            KawaseBlur.blur.render(() -> {
                hands.draw();
            });

            Outline.registerRenderCall(() -> {
                hands.draw();
            });


            GlStateManager.disableAlphaTest();
            GlStateManager.popMatrix();
        }
    }

    public static void setSaturation(float saturation) {
        float[] saturationMatrix = {0.3086f * (1.0f - saturation) + saturation, 0.6094f * (1.0f - saturation), 0.0820f * (1.0f - saturation), 0, 0, 0.3086f * (1.0f - saturation), 0.6094f * (1.0f - saturation) + saturation, 0.0820f * (1.0f - saturation), 0, 0, 0.3086f * (1.0f - saturation), 0.6094f * (1.0f - saturation), 0.0820f * (1.0f - saturation) + saturation, 0, 0, 0, 0, 0, 1, 0};
        GL11.glLoadMatrixf(saturationMatrix);
    }
}


