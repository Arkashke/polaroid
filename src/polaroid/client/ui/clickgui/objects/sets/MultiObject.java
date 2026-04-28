package polaroid.client.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.Polaroid;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.ui.clickgui.objects.ModuleObject;
import polaroid.client.ui.clickgui.objects.Object;

public class MultiObject extends Object {

    public ModeListSetting set;
    public ModuleObject object;

    public MultiObject(ModuleObject object, ModeListSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        
        int totalOptions = set.get().size();
        int enabledOptions = 0;
        for (BooleanSetting option : set.get()) {
            if (option.get()) enabledOptions++;
        }

        String enabledStatus = enabledOptions + "/" + totalOptions;
        float titleWidth = Fonts.otwindowsa.getWidth(enabledStatus, 6);
        
        // Title and counter
        Fonts.otwindowsa.drawText(stack, set.getName(), x, y, ColorUtils.rgba(200, 200, 200, 255), 6);
        Fonts.otwindowsa.drawText(stack, enabledStatus, x + width - titleWidth, y, ColorUtils.rgba(200, 200, 200, 255), 6);

        // Options
        float optionsY = y + 8f;
        int offset = 0;
        
        for (BooleanSetting mode : set.get()) {
            int textColor = mode.get() ? ColorUtils.rgba(255, 255, 255, 255) : ColorUtils.rgba(140, 140, 140, 255);
            Fonts.otwindowsa.drawText(stack, mode.getName(), x + offset, optionsY, textColor, 6);
            offset += Fonts.otwindowsa.getWidth(mode.getName(), 6) + 8;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!object.module.expanded) return;
        
        float optionsY = y + 8f;
        int offset = 0;
        
        for (BooleanSetting mode : set.get()) {
            float modeWidth = Fonts.otwindowsa.getWidth(mode.getName(), 6);
            if (isInRegion(mouseX, mouseY, x + offset, optionsY, modeWidth, 8)) {
                mode.set(!mode.get());
                return;
            }
            offset += modeWidth + 8;
        }
    }
    
    private boolean isInRegion(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
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


