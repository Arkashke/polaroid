package polaroid.client.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.Polaroid;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.ui.styles.StyleManager;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.ui.clickgui.objects.ModuleObject;
import polaroid.client.ui.clickgui.objects.Object;
import polaroid.client.utils.render.font.Fonts;

import java.awt.*;

public class BooleanObject extends Object {

    public ModuleObject object;
    public BooleanSetting set;
    public float enabledAnimation;

    public BooleanObject(ModuleObject object, BooleanSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        double max = !set.get() ? 0 : 8.5f;
        this.enabledAnimation = MathUtil.fast(enabledAnimation, (float) max, 10);
        int colorfont = ColorUtils.interpolateColor(ColorUtils.rgba(255, 255, 255, 100), Color.WHITE.getRGB(), enabledAnimation / 6.5f);
        
        // Checkbox
        DisplayUtils.drawRoundedRect(x, y, 8, 8, new Vector4f(2,2,2,2), ColorUtils.rgba(20, 21, 24, 145));
        
        int indicatorColor = set.get()
                ? new Color(50, 200, 100).getRGB()
                : new Color(180, 60, 60).getRGB();

        DisplayUtils.drawRoundedRect(x + 2f, y + 2f, 4, 4, new Vector4f(2,2,2,2), indicatorColor);
        
        // Text
        Fonts.otwindowsa.drawText(stack, set.getName(), x + 12, y + 2f, colorfont, 6);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (object.module.expanded) {
            if (mouseButton == 0) {
                if (isHovered(mouseX, mouseY)) {
                    set.set(!set.get());
                }
            }
        }
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }


}


