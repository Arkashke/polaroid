package polaroid.client.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.ui.clickgui.objects.ModuleObject;
import polaroid.client.ui.clickgui.objects.Object;
import polaroid.client.utils.render.font.Fonts;

public class ModeObject extends Object {
    public ModeSetting set;
    public ModuleObject object;

    public ModeObject(ModuleObject object, ModeSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        
        // Title
        Fonts.otwindowsa.drawText(stack, set.getName(), x, y, ColorUtils.rgba(200, 200, 200, 255), 6);

        // Options container
        float optionsY = y + 8f;
        int offset = 0;
        int i = 0;
        
        for (String mode : set.strings) {
            boolean isSelected = set.getIndex() == i;
            int textColor = isSelected ? ColorUtils.rgba(255, 255, 255, 255) : ColorUtils.rgba(140, 140, 140, 255);
            
            Fonts.otwindowsa.drawText(stack, mode, x + offset, optionsY, textColor, 6);
            offset += Fonts.otwindowsa.getWidth(mode, 6) + 8;
            i++;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!object.module.expanded) return;
        
        float optionsY = y + 8f;
        int offset = 0;
        int i = 0;
        
        for (String mode : set.strings) {
            float modeWidth = Fonts.otwindowsa.getWidth(mode, 6);
            if (MathUtil.isHovered(mouseX, mouseY, x + offset, optionsY, modeWidth, 8)) {
                set.setIndex(i);
                return;
            }
            offset += modeWidth + 8;
            i++;
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


