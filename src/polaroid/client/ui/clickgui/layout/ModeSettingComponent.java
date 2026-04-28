package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.Polaroid;

public class ModeSettingComponent extends SettingComponent {
    
    private static final float ITEM_HEIGHT = 8f;
    private static final float HEADER_HEIGHT = 9f;
    private static final float PADDING = 3f;
    private ModeSetting modeSetting;
    
    public ModeSettingComponent(ModeSetting setting, Module module) {
        super(setting, module);
        this.modeSetting = setting;
    }
    
    @Override
    protected float calculateHeight(float availableWidth) {
        // Считаем высоту с учетом переноса текста
        float currentX = 0;
        int rows = 1;
        float contentWidth = availableWidth - PADDING * 2;
        float pillHeight = 12f;
        float pillPadding = 6f;
        float fontSize = 5.5f;
        
        for (String mode : modeSetting.strings) {
            float textWidth = Fonts.otwindowsa.getWidth(mode, fontSize);
            float pillWidth = textWidth + pillPadding * 2;
            
            if (currentX + pillWidth > contentWidth && currentX > 0) {
                rows++;
                currentX = pillWidth + 3f;
            } else {
                currentX += pillWidth + 3f;
            }
        }
        
        return HEADER_HEIGHT + (rows * (pillHeight + 2f)) + PADDING * 2;
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (!isVisible()) return;
        
        // Фон для группы настроек - тёмный базовый
        DisplayUtils.drawRoundedRect(x, y, width, getHeight(), 
                new Vector4f(3.5f, 3.5f, 3.5f, 3.5f), ColorUtils.rgba(18, 18, 22, 90));
        
        // Заголовок (уменьшенный шрифт)
        Fonts.otwindowsa.drawText(stack, modeSetting.getName(), x + PADDING, y + PADDING, 
                ColorUtils.rgba(220, 220, 220, 255), 6.5f);
        
        // Цвет темы для активной опции
        int themeColor = Polaroid.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB();
        
        // Опции в виде pill (капсул) - компактные
        float currentX = 0;
        float currentY = y + HEADER_HEIGHT + PADDING;
        int i = 0;
        float contentWidth = width - PADDING * 2;
        float pillHeight = 12f;
        float pillPadding = 6f;
        float fontSize = 5.5f;
        
        for (String mode : modeSetting.strings) {
            boolean isSelected = modeSetting.getIndex() == i;
            
            float textWidth = Fonts.otwindowsa.getWidth(mode, fontSize);
            float pillWidth = textWidth + pillPadding * 2;
            
            // Если не влезает - переносим на новую строку
            if (currentX + pillWidth > contentWidth && currentX > 0) {
                currentX = 0;
                currentY += pillHeight + 2f;
            }
            
            float optionX = x + PADDING + currentX;
            float pillRadius = 3f;
            
            // Проверка hover
            boolean isHovered = mouseX >= optionX && mouseX <= optionX + pillWidth &&
                               mouseY >= currentY && mouseY <= currentY + pillHeight;
            
            if (isSelected) {
                // Активная опция - акцентный цвет
                int bgColor = isHovered ? 
                    ColorUtils.setAlpha(themeColor, 140) : 
                    ColorUtils.setAlpha(themeColor, 120);
                DisplayUtils.drawRoundedRect(optionX, currentY, pillWidth, pillHeight, 
                        new Vector4f(pillRadius, pillRadius, pillRadius, pillRadius), bgColor);
                
                // Белая обводка вокруг кнопки - увеличена в 2 раза (было 1.5f, стало 3.0f)
                DisplayUtils.drawRoundedRectOutline(optionX, currentY, pillWidth, pillHeight, pillRadius, 3.0f, ColorUtils.rgba(255, 255, 255, 50));
                
                // Белый текст
                Fonts.otwindowsa.drawText(stack, mode, 
                    optionX + pillPadding, currentY + (pillHeight - fontSize) / 2f, 
                    ColorUtils.rgba(255, 255, 255, 255), fontSize);
            } else {
                // Неактивная опция - тёмный фон
                int bgColor = isHovered ? 
                    ColorUtils.rgba(35, 35, 40, 255) : 
                    ColorUtils.rgba(28, 28, 32, 255);
                DisplayUtils.drawRoundedRect(optionX, currentY, pillWidth, pillHeight, 
                        new Vector4f(pillRadius, pillRadius, pillRadius, pillRadius), bgColor);
                
                // Белая обводка вокруг кнопки - увеличена в 2 раза (было 1.5f, стало 3.0f)
                DisplayUtils.drawRoundedRectOutline(optionX, currentY, pillWidth, pillHeight, pillRadius, 3.0f, ColorUtils.rgba(255, 255, 255, 50));
                
                // Серый текст
                int textColor = isHovered ? 
                    ColorUtils.rgba(180, 180, 180, 255) : 
                    ColorUtils.rgba(140, 140, 140, 255);
                Fonts.otwindowsa.drawText(stack, mode, 
                    optionX + pillPadding, currentY + (pillHeight - fontSize) / 2f, 
                    textColor, fontSize);
            }
            
            currentX += pillWidth + 3f;
            i++;
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;
        
        float currentX = 0;
        float currentY = y + HEADER_HEIGHT + PADDING;
        int i = 0;
        float contentWidth = width - PADDING * 2;
        float pillHeight = 12f;
        float pillPadding = 6f;
        float fontSize = 5.5f;
        
        for (String mode : modeSetting.strings) {
            float textWidth = Fonts.otwindowsa.getWidth(mode, fontSize);
            float pillWidth = textWidth + pillPadding * 2;
            
            // Если не влезает - переносим на новую строку
            if (currentX + pillWidth > contentWidth && currentX > 0) {
                currentX = 0;
                currentY += pillHeight + 2f;
            }
            
            float optionX = x + PADDING + currentX;
            
            if (mouseX >= optionX && mouseX <= optionX + pillWidth &&
                mouseY >= currentY && mouseY <= currentY + pillHeight) {
                modeSetting.setIndex(i);
                return true;
            }
            
            currentX += pillWidth + 3f;
            i++;
        }
        return false;
    }
}


