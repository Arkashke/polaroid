package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.math.MathUtil;
import java.awt.Color;

public class BooleanSettingComponent extends SettingComponent {
    
    private static final float HEIGHT = 12f;
    private BooleanSetting boolSetting;
    private float enabledAnimation = 0f;
    
    public BooleanSettingComponent(BooleanSetting setting, Module module) {
        super(setting, module);
        this.boolSetting = setting;
    }
    
    @Override
    protected float calculateHeight(float availableWidth) {
        return HEIGHT;
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (!isVisible()) return;
        
        enabledAnimation = MathUtil.fast(enabledAnimation, boolSetting.get() ? 8.5f : 0f, 10);
        int colorfont = ColorUtils.interpolateColor(
                ColorUtils.rgba(255, 255, 255, 100), 
                Color.WHITE.getRGB(), 
                enabledAnimation / 6.5f
        );
        
        // Идеально круглый индикатор через drawCircle
        int indicatorColor = boolSetting.get()
                ? new Color(50, 200, 100).getRGB()
                : new Color(180, 60, 60).getRGB();
        
        // Рисуем идеально круглый индикатор (радиус 3.5, центр в x+3.5, y+7)
        DisplayUtils.drawCircle(x + 3.5f, y + 7f, 7f, indicatorColor);
        
        // Text - выровнен по вертикали с кругом
        Fonts.otwindowsa.drawText(stack, boolSetting.getName(), x + 11, y + 4f, colorfont, 6);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;
        if (button == 0 && isHovered(mouseX, mouseY)) {
            boolSetting.set(!boolSetting.get());
            return true;
        }
        return false;
    }
}


