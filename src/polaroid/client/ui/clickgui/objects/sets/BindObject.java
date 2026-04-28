package polaroid.client.ui.clickgui.objects.sets;

import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.ui.clickgui.objects.ModuleObject;
import polaroid.client.ui.clickgui.objects.Object;
import polaroid.client.utils.render.font.Fonts;
import org.lwjgl.glfw.GLFW;

public class BindObject extends Object {

    public BindSetting set;
    public ModuleObject object;
    boolean bind;

    public boolean isBinding;

    private static final int MOUSE_BUTTON_3 = 3;
    private static final int MOUSE_BUTTON_4 = 4;

    @Override
    public void draw(MatrixStack matrixStack, int mouseX, int mouseY) {
        String bindString = bind ? "..." : (set.get() == 0 ? "NONE" : getKeyName(set.get()));

        if (bindString == null) {
            bindString = "";
        }

        bindString = bindString.replace("MOUSE", "M");
        bindString = bindString.replace("LEFT", "L");
        bindString = bindString.replace("RIGHT", "R");
        bindString = bindString.replace("CONTROL", "C");
        bindString = bindString.replace("SHIFT", "S");
        bindString = bindString.replace("_", "");

        String shortBindString = bindString.substring(0, Math.min(bindString.length(), 4));

        float width = Fonts.otwindowsa.getWidth(shortBindString, 6) + 4;

        DisplayUtils.drawRoundedRect(x + 12, y + 2, width, 8, 2, ColorUtils.rgba(20, 21, 24, 175));
        Fonts.otwindowsa.drawCenteredText(matrixStack, shortBindString, x + 12 + (width / 2), y + 4.5f, -1, 6);
        Fonts.otwindowsa.drawText(matrixStack, set.getName(), x + 12 + width + 3, y + 4.5f, -1, 6);
    }
    
    private String getKeyName(int key) {
        if (key < 0) {
            return "M" + (key + 100);
        }
        if (key == 0) {
            return "NONE"; // GLFW_KEY_UNKNOWN, invalid for glfwGetKeyName
        }
        String name = GLFW.glfwGetKeyName(key, 0);
        return name != null ? name.toUpperCase() : "KEY" + key;
    }


    public BindObject(ModuleObject object, BindSetting set) {
        this.object = object;
        this.set = set;
        setting = set;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (object.module.expanded) {
            if (isInRegion(mouseX, mouseY, x + 12, y + 2, width + 6, 13)) {
                if (bind && (mouseButton > 1 || mouseButton == MOUSE_BUTTON_3 || mouseButton == MOUSE_BUTTON_4)) {
                    set.set(-100 + mouseButton);
                    bind = false;

                }
                if (isHovered(mouseX, mouseY) && mouseButton == 0) {
                    bind = true;
                }
            }
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
        if (bind) {
            if (keyCode == 261 || keyCode == 259) {
                set.set(0);
                bind = false;
                return;
            }
            set.set(keyCode);
            bind = false;
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}

