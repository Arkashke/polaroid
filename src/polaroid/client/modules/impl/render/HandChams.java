package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.ui.styles.Style;
import polaroid.client.utils.CustomFramebuffer;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.shader.impl.Outline;
import lombok.Getter;
import net.minecraft.client.settings.PointOfView;
import org.lwjgl.opengl.GL11;

@ModuleSystem(
    name = "HandChams", 
    type = Category.Render, 
    server = ServerCategory.NO, 
    description = "Добавляет обводку и полупрозрачную заливку рук"
)
public class HandChams extends Module {

    // Настройка ебаной хуйни дя
    private final BooleanSetting outline = new BooleanSetting("Обводка", true);
    private final SliderSetting outlineWidth = new SliderSetting("Толщина обводки", 1.5f, 1.0f, 3.0f, 0.1f)
        .setVisible(() -> outline.get());
    
    private final BooleanSetting chams = new BooleanSetting("Заливка", true);
    private final SliderSetting chamsAlpha = new SliderSetting("Прозрачность заливки", 60.0f, 20.0f, 100.0f, 5.0f)
        .setVisible(() -> chams.get());
    
    private final BooleanSetting glow = new BooleanSetting("Свечение", true);
    private final SliderSetting glowRadius = new SliderSetting("Радиус свечения", 3.0f, 1.0f, 10.0f, 0.5f)
        .setVisible(() -> glow.get());

    @Getter
    private final CustomFramebuffer handsFramebuffer = new CustomFramebuffer(true).setLinear();
    private final CustomFramebuffer glowFramebuffer = new CustomFramebuffer(false).setLinear();

    public HandChams() {
        addSettings(outline, outlineWidth, chams, chamsAlpha, glow, glowRadius);
    }

    @Subscribe
    public void onRender(EventDisplay event) {
        if (event.getType() != EventDisplay.Type.HIGH) {
            return;
        }

        if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) {
            return;
        }

        int primaryColor = getPrimaryColor();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);

        GlStateManager.enableDepthTest();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);

        try {
            if (chams.get()) {
                renderChams(primaryColor);
            }

            if (glow.get()) {
                renderGlow(primaryColor);
            }

            if (outline.get()) {
                renderOutline(primaryColor);
            }
        } finally {
            GlStateManager.disableBlend();
            GlStateManager.disableAlphaTest();
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
    }


    private void renderChams(int color) {

        int alpha = (int) (chamsAlpha.get() * 2.55f);
        int chamsColor = ColorUtils.setAlpha(color, alpha);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ColorUtils.setColor(chamsColor);

        handsFramebuffer.draw();
    }


    private void renderGlow(int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        ColorUtils.setColor(color);

        float radius = glowRadius.get();
        int layers = (int) Math.ceil(radius);
        
        for (int i = 0; i < layers; i++) {
            float layerAlpha = (1.0f - (float) i / layers) * 0.3f;
            int glowColor = ColorUtils.setAlpha(color, (int) (layerAlpha * 255));
            ColorUtils.setColor(glowColor);
            
            handsFramebuffer.draw();
        }
        
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.popMatrix();
    }


    private void renderOutline(int color) {
        Outline.registerRenderCall(() -> {
            handsFramebuffer.draw();
        });

        int outlineRadius = (int) (outlineWidth.get() * 2);
        Outline.draw(outlineRadius, color);
    }

    private int getPrimaryColor() {
        try {
            Style currentStyle = Polaroid.getInstance().getStyleManager().getCurrentStyle();
            
            if (currentStyle != null && currentStyle.getFirstColor() != null) {
                return currentStyle.getFirstColor().getRGB();
            }
        } catch (Exception e) {
        }
        
        return Theme.getColor(0);
    }

    @Override
    public boolean onDisable() {
        if (handsFramebuffer != null) {
            handsFramebuffer.framebufferClear(false);
        }
        if (glowFramebuffer != null) {
            glowFramebuffer.framebufferClear(false);
        }
        return super.onDisable();
    }
}


