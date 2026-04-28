package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.impl.BindSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import org.lwjgl.glfw.GLFW;
import java.awt.Color;

public class BindSettingComponent extends SettingComponent {
    
    private static final float HEIGHT = 12f;
    private static BindSettingComponent currentlyBinding = null;
    private BindSetting bindSetting;
    private boolean binding = false;
    
    public BindSettingComponent(BindSetting setting, Module module) {
        super(setting, module);
        this.bindSetting = setting;
    }
    
    @Override
    protected float calculateHeight(float availableWidth) {
        return HEIGHT;
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (!isVisible()) return;
        
        String bindString = binding ? "..." : (bindSetting.get() == 0 ? "NONE" : getKeyName(bindSetting.get()));
        if (bindString == null) bindString = "";
        
        // Не сокращаем имена клавиш
        float bindWidth = Fonts.otwindowsa.getWidth(bindString, 6) + 6;
        
        // Более заметный фон для кнопки бинда
        DisplayUtils.drawRoundedRect(x, y + 2, bindWidth, 9, 2, ColorUtils.rgba(40, 45, 50, 220));
        
        // Обводка вокруг кнопки бинда - уменьшена на 0.5 (было 3.0f, стало 2.5f)
        DisplayUtils.drawRoundedRectOutline(x, y + 2, bindWidth, 9, 2, 2.5f, ColorUtils.rgba(255, 255, 255, 50));
        
        // Текст по центру кнопки - поднят на 0.3 пикселя (было 4.5f, стало 4.2f)
        Fonts.otwindowsa.drawCenteredText(stack, bindString, x + (bindWidth / 2), y + 4.2f, -1, 6);
        
        // Название настройки - поднято на 2 пикселя (было 5f, стало 3f)
        Fonts.otwindowsa.drawText(stack, bindSetting.getName(), x + bindWidth + 4, y + 3f, -1, 6);
    }
    
    private String getKeyName(int key) {
        if (key < 0) return "M" + (key + 100);
        
        // Специальные клавиши
        switch (key) {
            case 256: return "ESC";
            case 257: return "ENTER";
            case 258: return "TAB";
            case 259: return "BACKSPACE";
            case 260: return "INSERT";
            case 261: return "DELETE";
            case 262: return "RIGHT";
            case 263: return "LEFT";
            case 264: return "DOWN";
            case 265: return "UP";
            case 340: return "LSHIFT";
            case 344: return "RSHIFT";
            case 341: return "LCTRL";
            case 345: return "RCTRL";
            case 342: return "LALT";
            case 346: return "RALT";
            case 32: return "SPACE";
            case 290: return "F1";
            case 291: return "F2";
            case 292: return "F3";
            case 293: return "F4";
            case 294: return "F5";
            case 295: return "F6";
            case 296: return "F7";
            case 297: return "F8";
            case 298: return "F9";
            case 299: return "F10";
            case 300: return "F11";
            case 301: return "F12";
            
            // Буквы A-Z (всегда английские)
            case 65: return "A";
            case 66: return "B";
            case 67: return "C";
            case 68: return "D";
            case 69: return "E";
            case 70: return "F";
            case 71: return "G";
            case 72: return "H";
            case 73: return "I";
            case 74: return "J";
            case 75: return "K";
            case 76: return "L";
            case 77: return "M";
            case 78: return "N";
            case 79: return "O";
            case 80: return "P";
            case 81: return "Q";
            case 82: return "R";
            case 83: return "S";
            case 84: return "T";
            case 85: return "U";
            case 86: return "V";
            case 87: return "W";
            case 88: return "X";
            case 89: return "Y";
            case 90: return "Z";
            
            // Цифры 0-9
            case 48: return "0";
            case 49: return "1";
            case 50: return "2";
            case 51: return "3";
            case 52: return "4";
            case 53: return "5";
            case 54: return "6";
            case 55: return "7";
            case 56: return "8";
            case 57: return "9";
            
            // Символы
            case 39: return "'";
            case 44: return ",";
            case 45: return "-";
            case 46: return ".";
            case 47: return "/";
            case 59: return ";";
            case 61: return "=";
            case 91: return "[";
            case 92: return "\\";
            case 93: return "]";
            case 96: return "`";
            
            // Numpad
            case 320: return "NUM0";
            case 321: return "NUM1";
            case 322: return "NUM2";
            case 323: return "NUM3";
            case 324: return "NUM4";
            case 325: return "NUM5";
            case 326: return "NUM6";
            case 327: return "NUM7";
            case 328: return "NUM8";
            case 329: return "NUM9";
            case 330: return "NUM.";
            case 331: return "NUM/";
            case 332: return "NUM*";
            case 333: return "NUM-";
            case 334: return "NUM+";
            case 335: return "NUMENTER";
        }
        
        // Если не нашли - возвращаем код клавиши
        return "KEY" + key;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;
        if (isHovered(mouseX, mouseY)) {
            if (button > 1) {
                bindSetting.set(-100 + button);
                binding = false;
                if (currentlyBinding == this) {
                    currentlyBinding = null;
                }
            } else if (button == 0) {
                // Отменяем предыдущий компонент в режиме binding
                if (currentlyBinding != null && currentlyBinding != this) {
                    currentlyBinding.binding = false;
                }
                binding = true;
                currentlyBinding = this;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            // ESC, DELETE или BACKSPACE - убираем бинд
            if (keyCode == 256 || keyCode == 261 || keyCode == 259) {
                bindSetting.set(0);
            } else {
                bindSetting.set(keyCode);
            }
            binding = false;
            if (currentlyBinding == this) {
                currentlyBinding = null;
            }
            // Предотвращаем закрытие ClickGUI при ESC во время биндинга
            // Возвращаем true чтобы событие не распространялось дальше
        }
    }
    
    public static boolean isAnyBinding() {
        return currentlyBinding != null;
    }
}


