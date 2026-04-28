package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.settings.impl.ColorSetting;
import polaroid.client.utils.client.IMinecraft;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.math.MathUtil;
import java.awt.Color;

/**
 * Отдельное окно для выбора цвета
 * Открывается при клике на ColorSetting в режиме Custom
 */
public class ColorPickerWindow implements IMinecraft {
    
    private static final float WINDOW_WIDTH = 120f;
    private static final float WINDOW_HEIGHT = 110f;
    private static final float PADDING = 5f;
    
    private static ColorPickerWindow currentlyOpen = null;
    
    private ColorSetting colorSetting;
    private boolean visible = false;
    
    private float windowX;
    private float windowY;
    
    private float hue = 0f;
    private float saturation = 1f;
    private float brightness = 1f;
    private float alpha = 1f;
    
    private boolean draggingWindow = false;
    private boolean draggingHue = false;
    private boolean draggingSaturation = false;
    private boolean draggingBrightness = false;
    private boolean draggingAlpha = false;
    private boolean draggingColorPicker = false;
    
    private float dragOffsetX;
    private float dragOffsetY;
    
    public ColorPickerWindow(ColorSetting setting) {
        this.colorSetting = setting;
        updateFromColor();
    }
    
    public void open(float x, float y) {
        // Закрываем предыдущее открытое окно
        if (currentlyOpen != null && currentlyOpen != this) {
            currentlyOpen.close();
        }
        
        this.visible = true;
        this.windowX = x;
        this.windowY = y;
        currentlyOpen = this;
        updateFromColor();
    }
    
    public void close() {
        this.visible = false;
        if (currentlyOpen == this) {
            currentlyOpen = null;
        }
        draggingWindow = false;
        draggingHue = false;
        draggingSaturation = false;
        draggingBrightness = false;
        draggingAlpha = false;
        draggingColorPicker = false;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    private void updateFromColor() {
        Color color = new Color(colorSetting.get());
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = color.getAlpha() / 255f;
    }
    
    private void updateColor() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        Color color = new Color(rgb);
        int finalColor = ColorUtils.rgba(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
        colorSetting.set(finalColor);
    }
    
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (!visible) return;
        
        // Обработка перетаскивания окна
        if (draggingWindow) {
            windowX = mouseX - dragOffsetX;
            windowY = mouseY - dragOffsetY;
        }
        
        // Фон окна выбора цвета (блюр удален, оптимальная прозрачность)
        DisplayUtils.drawRoundedRect(windowX, windowY, WINDOW_WIDTH, WINDOW_HEIGHT, 
                new Vector4f(6f, 6f, 6f, 6f), ColorUtils.rgba(16, 16, 16, 185));;
        
        DisplayUtils.drawRoundedRect(windowX, windowY, WINDOW_WIDTH, WINDOW_HEIGHT, 
                new Vector4f(6f, 6f, 6f, 6f), ColorUtils.rgba(16, 16, 16, 150));
        
        // Заголовок
        Fonts.otwindowsa.drawText(stack, colorSetting.getName(), windowX + PADDING, windowY + PADDING, 
                ColorUtils.rgba(200, 200, 200, 255), 6);
        
        // Кнопка закрытия
        float closeX = windowX + WINDOW_WIDTH - 12;
        float closeY = windowY + PADDING - 1;
        boolean hoveringClose = mouseX >= closeX && mouseX <= closeX + 10 && 
                               mouseY >= closeY && mouseY <= closeY + 10;
        
        Fonts.otwindowsa.drawText(stack, "X", closeX + 2, closeY + 1, 
                hoveringClose ? ColorUtils.rgba(255, 100, 100, 255) : ColorUtils.rgba(180, 180, 180, 255), 6);
        
        float contentY = windowY + 14;
        
        // Квадратный color picker
        float pickerSize = WINDOW_WIDTH - PADDING * 2;
        float pickerHeight = 50f;
        
        drawColorPicker(windowX + PADDING, contentY, pickerSize, pickerHeight);
        
        // Обработка перетаскивания color picker
        if (draggingColorPicker) {
            float relX = Math.max(0, Math.min(1, (mouseX - (windowX + PADDING)) / pickerSize));
            float relY = Math.max(0, Math.min(1, (mouseY - contentY) / pickerHeight));
            saturation = relX;
            brightness = 1f - relY;
            updateColor();
        }
        
        contentY += pickerHeight + 4;
        
        // Hue slider
        float sliderWidth = WINDOW_WIDTH - PADDING * 2;
        
        if (draggingHue) {
            hue = Math.max(0, Math.min(1, (float)(mouseX - (windowX + PADDING)) / sliderWidth));
            updateColor();
        }
        
        drawHueSlider(windowX + PADDING, contentY, sliderWidth);
        contentY += 8;
        
        // Alpha slider
        if (draggingAlpha) {
            alpha = Math.max(0, Math.min(1, (float)(mouseX - (windowX + PADDING)) / sliderWidth));
            updateColor();
        }
        
        int currentColor = Color.HSBtoRGB(hue, saturation, brightness);
        drawAlphaSlider(windowX + PADDING, contentY, sliderWidth, currentColor);
        contentY += 8;
        
        // Превью цвета
        DisplayUtils.drawRoundedRect(windowX + PADDING, contentY, sliderWidth, 10, 
                new Vector4f(3f, 3f, 3f, 3f), colorSetting.get());
        DisplayUtils.drawRoundedRectOutline(windowX + PADDING, contentY, sliderWidth, 10, 
                3f, 0.5f, ColorUtils.rgba(60, 60, 65, 150));
        
        // RGB значения
        Color finalColor = new Color(colorSetting.get(), true);
        String rgbText = String.format("%d,%d,%d,%d", 
                finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
        Fonts.otwindowsa.drawText(stack, rgbText, windowX + PADDING + 2, contentY + 2, 
                ColorUtils.rgba(255, 255, 255, 255), 4.5f);
    }
    
    private void drawColorPicker(float x, float y, float width, float height) {
        // Рисуем градиент saturation (слева направо) и brightness (сверху вниз)
        int segments = 30;
        float segmentWidth = width / segments;
        float segmentHeight = height / segments;
        
        for (int i = 0; i < segments; i++) {
            for (int j = 0; j < segments; j++) {
                float sat = (float) i / segments;
                float bright = 1f - (float) j / segments;
                int color = Color.HSBtoRGB(hue, sat, bright);
                
                DisplayUtils.drawRoundedRect(
                        x + i * segmentWidth, 
                        y + j * segmentHeight, 
                        segmentWidth + 0.5f, 
                        segmentHeight + 0.5f, 
                        new Vector4f(0, 0, 0, 0), 
                        color
                );
            }
        }
        
        // Рамка
        DisplayUtils.drawRoundedRectOutline(x, y, width, height, 3f, 0.5f, 
                ColorUtils.rgba(60, 60, 65, 200));
        
        // Курсор выбора (маленький круг)
        float cursorX = x + saturation * width;
        float cursorY = y + (1f - brightness) * height;
        
        // Внешний круг (белый)
        DisplayUtils.drawCircle(cursorX, cursorY, 4f, ColorUtils.rgba(255, 255, 255, 255));
        // Внутренний круг (текущий цвет)
        DisplayUtils.drawCircle(cursorX, cursorY, 2.5f, Color.HSBtoRGB(hue, saturation, brightness));
    }
    
    private void drawHueSlider(float x, float y, float width) {
        // Рисуем градиент hue
        int segments = 30;
        float segmentWidth = width / segments;
        
        for (int i = 0; i < segments; i++) {
            float hueValue = (float) i / segments;
            int color = Color.HSBtoRGB(hueValue, 1f, 1f);
            DisplayUtils.drawRoundedRect(x + i * segmentWidth, y, segmentWidth + 0.5f, 4, 
                    new Vector4f(0, 0, 0, 0), color);
        }
        
        // Рамка
        DisplayUtils.drawRoundedRectOutline(x, y, width, 4, 2f, 0.5f, 
                ColorUtils.rgba(60, 60, 65, 150));
        
        // Ползунок
        float thumbX = x + hue * width - 1.5f;
        DisplayUtils.drawRoundedRect(thumbX, y - 1, 3, 6, new Vector4f(1.5f, 1.5f, 1.5f, 1.5f), 
                ColorUtils.rgba(255, 255, 255, 255));
    }
    
    private void drawAlphaSlider(float x, float y, float width, int baseColor) {
        // Фон шахматной доски
        drawCheckerboard(x, y, width, 4);
        
        // Рисуем градиент alpha
        int segments = 20;
        float segmentWidth = width / segments;
        
        for (int i = 0; i < segments; i++) {
            float alphaValue = (float) i / segments;
            int color = ColorUtils.setAlpha(baseColor, (int)(alphaValue * 255));
            DisplayUtils.drawRoundedRect(x + i * segmentWidth, y, segmentWidth + 0.5f, 4, 
                    new Vector4f(0, 0, 0, 0), color);
        }
        
        // Рамка
        DisplayUtils.drawRoundedRectOutline(x, y, width, 4, 2f, 0.5f, 
                ColorUtils.rgba(60, 60, 65, 150));
        
        // Ползунок
        float thumbX = x + alpha * width - 1.5f;
        DisplayUtils.drawRoundedRect(thumbX, y - 1, 3, 6, new Vector4f(1.5f, 1.5f, 1.5f, 1.5f), 
                ColorUtils.rgba(255, 255, 255, 255));
    }
    
    private void drawCheckerboard(float x, float y, float width, float height) {
        int checkSize = 3;
        int checksX = (int) Math.ceil(width / checkSize);
        int checksY = (int) Math.ceil(height / checkSize);
        
        for (int i = 0; i < checksX; i++) {
            for (int j = 0; j < checksY; j++) {
                if ((i + j) % 2 == 0) {
                    DisplayUtils.drawRoundedRect(
                            x + i * checkSize, 
                            y + j * checkSize, 
                            checkSize, 
                            checkSize, 
                            new Vector4f(0, 0, 0, 0), 
                            ColorUtils.rgba(180, 180, 180, 255)
                    );
                } else {
                    DisplayUtils.drawRoundedRect(
                            x + i * checkSize, 
                            y + j * checkSize, 
                            checkSize, 
                            checkSize, 
                            new Vector4f(0, 0, 0, 0), 
                            ColorUtils.rgba(130, 130, 130, 255)
                    );
                }
            }
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        
        // ВАЖНО: Сначала проверяем что клик внутри окна, чтобы блокировать клики под окном
        boolean insideWindow = mouseX >= windowX && mouseX <= windowX + WINDOW_WIDTH && 
                               mouseY >= windowY && mouseY <= windowY + WINDOW_HEIGHT;
        
        if (!insideWindow) {
            return false;
        }
        
        if (button == 0) {
            // Проверка клика по кнопке закрытия
            float closeX = windowX + WINDOW_WIDTH - 12;
            float closeY = windowY + PADDING - 1;
            
            if (mouseX >= closeX && mouseX <= closeX + 10 && 
                mouseY >= closeY && mouseY <= closeY + 10) {
                close();
                return true;
            }
            
            // Проверка клика по заголовку для перетаскивания
            if (mouseY >= windowY && mouseY <= windowY + 14) {
                draggingWindow = true;
                dragOffsetX = (float) (mouseX - windowX);
                dragOffsetY = (float) (mouseY - windowY);
                return true;
            }
            
            float contentY = windowY + 14;
            float pickerSize = WINDOW_WIDTH - PADDING * 2;
            float pickerHeight = 50f;
            
            // Проверка клика по color picker
            if (mouseX >= windowX + PADDING && mouseX <= windowX + PADDING + pickerSize && 
                mouseY >= contentY && mouseY <= contentY + pickerHeight) {
                draggingColorPicker = true;
                float relX = Math.max(0, Math.min(1, (float)(mouseX - (windowX + PADDING)) / pickerSize));
                float relY = Math.max(0, Math.min(1, (float)(mouseY - contentY) / pickerHeight));
                saturation = relX;
                brightness = 1f - relY;
                updateColor();
                return true;
            }
            
            contentY += pickerHeight + 4;
            
            // Проверка клика по hue slider
            float sliderWidth = WINDOW_WIDTH - PADDING * 2;
            if (mouseX >= windowX + PADDING && mouseX <= windowX + PADDING + sliderWidth && 
                mouseY >= contentY && mouseY <= contentY + 4) {
                draggingHue = true;
                hue = Math.max(0, Math.min(1, (float)(mouseX - (windowX + PADDING)) / sliderWidth));
                updateColor();
                return true;
            }
            
            contentY += 8;
            
            // Проверка клика по alpha slider
            if (mouseX >= windowX + PADDING && mouseX <= windowX + PADDING + sliderWidth && 
                mouseY >= contentY && mouseY <= contentY + 4) {
                draggingAlpha = true;
                alpha = Math.max(0, Math.min(1, (float)(mouseX - (windowX + PADDING)) / sliderWidth));
                updateColor();
                return true;
            }
        }
        
        // Клик внутри окна - блокируем клики под окном
        return true;
    }
    
    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingWindow = false;
        draggingHue = false;
        draggingSaturation = false;
        draggingBrightness = false;
        draggingAlpha = false;
        draggingColorPicker = false;
    }
    
    public boolean isHovered(double mouseX, double mouseY) {
        return visible && 
               mouseX >= windowX && mouseX <= windowX + WINDOW_WIDTH && 
               mouseY >= windowY && mouseY <= windowY + WINDOW_HEIGHT;
    }
}


