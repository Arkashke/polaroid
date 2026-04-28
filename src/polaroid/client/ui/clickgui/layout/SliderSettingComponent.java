package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.math.MathUtil;
import java.awt.Color;

public class SliderSettingComponent extends SettingComponent {
    
    private static final float HEIGHT = 18f;
    private static final float PADDING = 3f;
    private SliderSetting sliderSetting;
    private boolean sliding = false;
    private float animatedVal = 0f;
    
    public SliderSettingComponent(SliderSetting setting, Module module) {
        super(setting, module);
        this.sliderSetting = setting;
    }
    
    @Override
    protected float calculateHeight(float availableWidth) {
        return HEIGHT;
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (!isVisible()) return;
        
        // Фон для группы настроек (alpha = 90)
        DisplayUtils.drawRoundedRect(x, y, width, HEIGHT, 
                new Vector4f(3.5f, 3.5f, 3.5f, 3.5f), ColorUtils.rgba(25, 26, 31, 90));
        
        if (sliding) {
            float value = (float) ((mouseX - (x + PADDING)) / (width - PADDING * 2) * (sliderSetting.max - sliderSetting.min) + sliderSetting.min);
            value = (float) MathUtil.round(value, sliderSetting.increment);
            value = Math.max(sliderSetting.min, Math.min(sliderSetting.max, value));
            sliderSetting.set(value);
        }
        
        float sliderWidth = ((sliderSetting.get() - sliderSetting.min) / (sliderSetting.max - sliderSetting.min)) * (width - PADDING * 2);
        animatedVal = MathUtil.fast(animatedVal, sliderWidth, 11);
        
        // Labels (размер 7) - ВЫШЕ ползунка
        Fonts.otwindowsa.drawText(stack, sliderSetting.getName(), x + PADDING, y + PADDING, Color.WHITE.getRGB(), 7);
        String valueStr = String.valueOf(sliderSetting.get());
        Fonts.otwindowsa.drawText(stack, valueStr, 
                x + width - Fonts.otwindowsa.getWidth(valueStr, 7) - PADDING, y + PADDING, Color.WHITE.getRGB(), 7);
        
        // Background track - НИЖЕ текста
        DisplayUtils.drawRoundedRect(x + PADDING, y + 13f, width - PADDING * 2, 2f, 
                new Vector4f(1,1,1,1), ColorUtils.rgba(40, 40, 40, 200));
        // Filled track
        DisplayUtils.drawRoundedRect(x + PADDING, y + 13f, animatedVal, 2f, 
                new Vector4f(1,1,1,1), ColorUtils.rgba(255, 255, 255, 255));
        // Thumb
        DisplayUtils.drawRoundedRect(x + PADDING + animatedVal - 2, y + 11f, 4, 6f, 
                new Vector4f(2,2,2,2), -1);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;
        if (button == 0 && isHovered(mouseX, mouseY)) {
            sliding = true;
            return true;
        }
        return false;
    }
    
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        sliding = false;
    }
}


