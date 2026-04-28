package polaroid.client.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.settings.impl.StringSetting;
import polaroid.client.ui.clickgui.objects.ModuleObject;
import polaroid.client.ui.clickgui.objects.Object;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class StringObject extends Object {

    public ModuleObject object;
    public StringSetting set;
    private boolean editing = false;
    private String tempValue = "";

    public StringObject(ModuleObject object, StringSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
        this.tempValue = set.get();
    }

    @Override
    public void draw(MatrixStack stack, int mouseX, int mouseY) {
        super.draw(stack, mouseX, mouseY);
        
        // Background
        DisplayUtils.drawRoundedRect(x, y, width, 16, new Vector4f(2,2,2,2), ColorUtils.rgba(20, 21, 24, 145));
        
        // Label
        Fonts.otwindowsa.drawText(stack, set.getName() + ":", x + 4, y + 3, Color.WHITE.getRGB(), 6);
        
        // Value box
        float labelWidth = Fonts.otwindowsa.getWidth(set.getName() + ":", 6);
        float boxX = x + labelWidth + 8;
        float boxWidth = width - labelWidth - 12;
        
        int boxColor = editing 
                ? ColorUtils.rgba(40, 120, 200, 180)
                : ColorUtils.rgba(30, 31, 34, 180);
        
        DisplayUtils.drawRoundedRect(boxX, y + 2, boxWidth, 12, new Vector4f(2,2,2,2), boxColor);
        
        // Display value
        String displayValue = editing ? tempValue + "_" : set.get();
        if (displayValue.isEmpty()) {
            displayValue = editing ? "_" : set.getDescription();
        }
        
        // Truncate if too long
        float maxTextWidth = boxWidth - 4;
        while (Fonts.otwindowsa.getWidth(displayValue, 5.5f) > maxTextWidth && displayValue.length() > 1) {
            displayValue = displayValue.substring(0, displayValue.length() - 1);
        }
        
        int textColor = editing ? Color.WHITE.getRGB() : ColorUtils.rgba(200, 200, 200, 255);
        Fonts.otwindowsa.drawText(stack, displayValue, boxX + 2, y + 5, textColor, 5.5f);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (object.module.expanded) {
            if (mouseButton == 0) {
                if (isHovered(mouseX, mouseY)) {
                    editing = !editing;
                    if (editing) {
                        tempValue = set.get();
                    } else {
                        set.set(tempValue);
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (editing) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                set.set(tempValue);
                editing = false;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                tempValue = set.get();
                editing = false;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!tempValue.isEmpty()) {
                    tempValue = tempValue.substring(0, tempValue.length() - 1);
                }
            }
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (editing) {
            if (set.isOnlyNumber()) {
                if (Character.isDigit(codePoint) || codePoint == '.' || codePoint == '-') {
                    tempValue += codePoint;
                }
            } else {
                if (codePoint >= 32 && codePoint < 127) { // Printable ASCII
                    tempValue += codePoint;
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
}


