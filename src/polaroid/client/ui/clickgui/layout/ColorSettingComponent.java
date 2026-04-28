package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.impl.ColorSetting;
import polaroid.client.utils.client.IMinecraft;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import java.awt.Color;

public class ColorSettingComponent extends SettingComponent implements IMinecraft {
    
    private static final float HEIGHT = 12f;
    
    private ColorSetting colorSetting;
    private ColorPickerWindow colorPickerWindow;
    
    public ColorSettingComponent(ColorSetting setting, Module module) {
        super(setting, module);
        this.colorSetting = setting;
        this.colorPickerWindow = new ColorPickerWindow(setting);
    }
    
    @Override
    public float measure(float availableWidth) {
        if (!isVisible()) {
            measuredHeight = 0;
            return 0;
        }
        measuredHeight = HEIGHT;
        return HEIGHT;
    }
    
    @Override
    protected float calculateHeight(float availableWidth) {
        return HEIGHT;
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (!isVisible()) return;
        
        // Компактный вид - только название и квадратик с цветом
        Fonts.otwindowsa.drawText(stack, colorSetting.getName(), x, y + 3f, -1, 6);
        
        // Квадратик с цветом
        DisplayUtils.drawRoundedRect(x + width - 12, y + 1, 10, 10, 2.5F, colorSetting.get());
        // Белая обводка вокруг квадратика цвета - увеличена в 2 раза (было 1.5f, стало 3.0f)
        DisplayUtils.drawRoundedRectOutline(x + width - 12, y + 1, 10, 10, 2.5F, 3.0f, 
                ColorUtils.rgba(255, 255, 255, 50));
        
        // НЕ рендерим окно здесь - оно рендерится в Window.renderColorPickers()
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;
        
        // Если окно color picker открыто, передаем клик ему
        if (colorPickerWindow.isVisible()) {
            return colorPickerWindow.mouseClicked(mouseX, mouseY, button);
        }
        
        // Открытие color picker на ЛКМ или ПКМ
        if ((button == 0 || button == 1) && isMouseOver(mouseX, mouseY)) {
            // Открываем окно справа от компонента
            float windowX = x + width + 3;
            float windowY = y;
            
            // Проверяем чтобы окно не выходило за пределы экрана
            if (windowX + 120 > mc.getMainWindow().getScaledWidth()) {
                windowX = x - 120 - 3; // Открываем слева если не помещается справа
            }
            if (windowY + 110 > mc.getMainWindow().getScaledHeight()) {
                windowY = mc.getMainWindow().getScaledHeight() - 110 - 5;
            }
            
            colorPickerWindow.open(windowX, windowY);
            return true;
        }
        
        return false;
    }
    
    private boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (colorPickerWindow.isVisible()) {
            colorPickerWindow.mouseReleased(mouseX, mouseY, button);
        }
    }
    
    public ColorPickerWindow getColorPickerWindow() {
        return colorPickerWindow;
    }
}


