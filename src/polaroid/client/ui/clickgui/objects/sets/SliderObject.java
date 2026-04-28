package polaroid.client.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.ui.clickgui.objects.ModuleObject;
import polaroid.client.ui.clickgui.objects.Object;
import polaroid.client.utils.render.font.Fonts;

import java.awt.*;

public class SliderObject extends Object {

    public ModuleObject object;
    public SliderSetting set;
    public boolean sliding;

    public float animatedVal;
    public float animatedThumbX;

    public SliderObject(ModuleObject object, SliderSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
        animatedThumbX = x + 10 + 3 + ((set.get() - set.min) / (set.max - set.min)) * (width - 20);
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);

        if (sliding) {
            float value = (float) ((mouseX - (x + 3)) / (width - 6) * (set.max - set.min) + set.min);
            value = (float) MathUtil.round(value, set.increment);
            set.set(value);
        }

        float sliderWidth = ((set.get() - set.min) / (set.max - set.min)) * (width - 6);
        animatedVal = MathUtil.fast(animatedVal, sliderWidth, 11);

        // Background track
        DisplayUtils.drawRoundedRect(x, y + 6f, width, 2f, new Vector4f(1,1,1,1), ColorUtils.rgba(40, 40, 40, 200));
        // Filled track
        DisplayUtils.drawRoundedRect(x, y + 6f, animatedVal, 2f, new Vector4f(1,1,1,1), ColorUtils.rgba(255, 255, 255, 255));
        // Thumb
        DisplayUtils.drawRoundedRect(x + animatedVal - 2, y + 4f, 4, 6f, new Vector4f(2,2,2,2), -1);

        // Labels
        Fonts.otwindowsa.drawText(stack, set.getName(), x, y, Color.WHITE.getRGB(), 6);
        String valueStr = String.valueOf(set.get());
        Fonts.otwindowsa.drawText(stack, valueStr, x + width - Fonts.otwindowsa.getWidth(valueStr, 6), y, Color.WHITE.getRGB(), 6);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (object.module.expanded) {
            if (isHovered(mouseX, mouseY)) {
                sliding = true;
            }
        }
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {

    }

    @Override
    public void exit() {
        super.exit();
        sliding = false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        sliding = false;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}


