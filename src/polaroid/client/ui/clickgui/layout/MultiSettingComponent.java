package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.Polaroid;

public class MultiSettingComponent extends SettingComponent {
    
    private static final float ITEM_HEIGHT = 8f;
    private static final float HEADER_HEIGHT = 9f;
    private static final float PADDING = 3f;
    private ModeListSetting multiSetting;
    
    public MultiSettingComponent(ModeListSetting setting, Module module) {
        super(setting, module);
        this.multiSetting = setting;
    }
    
    @Override
    protected float calculateHeight(float availableWidth) {
        // Считаем высоту с учетом переноса текста
        float currentX = 0;
        int rows = 1;
        float contentWidth = availableWidth - PADDING * 2;
        float pillHeight = 12f; // Уменьшил с 16f
        float pillPadding = 6f; // Уменьшил с 8f
        float fontSize = 5.5f;
        
        for (BooleanSetting option : multiSetting.get()) {
            float textWidth = Fonts.otwindowsa.getWidth(option.getName(), fontSize);
            float pillWidth = textWidth + pillPadding * 2;
            
            if (currentX + pillWidth > contentWidth && currentX > 0) {
                rows++;
                currentX = pillWidth + 3f; // Уменьшил с 4f
            } else {
                currentX += pillWidth + 3f;
            }
        }
        
        return HEADER_HEIGHT + (rows * (pillHeight + 2f)) + PADDING * 2; // Уменьшил отступ с 3f до 2f
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (!isVisible()) return;
        
        // Фон для группы настроек - тёмный базовый
        DisplayUtils.drawRoundedRect(x, y, width, getHeight(), 
                new Vector4f(3.5f, 3.5f, 3.5f, 3.5f), ColorUtils.rgba(18, 18, 22, 90));
        
        int totalOptions = multiSetting.get().size();
        int enabledOptions = 0;
        for (BooleanSetting option : multiSetting.get()) {
            if (option.get()) enabledOptions++;
        }
        
        String enabledStatus = enabledOptions + "/" + totalOptions;
        float titleWidth = Fonts.otwindowsa.getWidth(enabledStatus, 6.5f);
        
        // Заголовок и счётчик (уменьшенный шрифт)
        Fonts.otwindowsa.drawText(stack, multiSetting.getName(), x + PADDING, y + PADDING, 
                ColorUtils.rgba(220, 220, 220, 255), 6.5f);
        Fonts.otwindowsa.drawText(stack, enabledStatus, x + width - titleWidth - PADDING, y + PADDING, 
                ColorUtils.rgba(160, 160, 160, 255), 6.5f);
        
        // Цвет темы для активных опций
        int themeColor = Polaroid.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB();
        
        // Опции в виде pill (капсул) - компактные
        float currentX = 0;
        float currentY = y + HEADER_HEIGHT + PADDING;
        float contentWidth = width - PADDING * 2;
        float pillHeight = 12f; // Уменьшил с 16f до 12f
        float pillPadding = 6f; // Уменьшил с 8f до 6f
        float fontSize = 5.5f; // Уменьшил с 6f до 5.5f
        
        for (BooleanSetting mode : multiSetting.get()) {
            float textWidth = Fonts.otwindowsa.getWidth(mode.getName(), fontSize);
            float pillWidth = textWidth + pillPadding * 2;
            
            // Если не влезает - переносим на новую строку
            if (currentX + pillWidth > contentWidth && currentX > 0) {
                currentX = 0;
                currentY += pillHeight + 2f; // Уменьшил отступ с 3f до 2f
            }
            
            float optionX = x + PADDING + currentX;
            float pillRadius = 3f; // Небольшое закругление
            
            // Проверка hover
            boolean isHovered = mouseX >= optionX && mouseX <= optionX + pillWidth &&
                               mouseY >= currentY && mouseY <= currentY + pillHeight;
            
            if (mode.get()) {
                // Включённая опция - акцентный цвет
                int bgColor = isHovered ? 
                    ColorUtils.setAlpha(themeColor, 140) : 
                    ColorUtils.setAlpha(themeColor, 120);
                DisplayUtils.drawRoundedRect(optionX, currentY, pillWidth, pillHeight, 
                        new Vector4f(pillRadius, pillRadius, pillRadius, pillRadius), bgColor);
                
                // Белая обводка вокруг кнопки - увеличена в 2 раза (было 1.5f, стало 3.0f)
                DisplayUtils.drawRoundedRectOutline(optionX, currentY, pillWidth, pillHeight, pillRadius, 3.0f, ColorUtils.rgba(255, 255, 255, 50));
                
                // Белый текст
                Fonts.otwindowsa.drawText(stack, mode.getName(), 
                    optionX + pillPadding, currentY + (pillHeight - fontSize) / 2f, 
                    ColorUtils.rgba(255, 255, 255, 255), fontSize);
            } else {
                // Выключённая опция - тёмный фон
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
                Fonts.otwindowsa.drawText(stack, mode.getName(), 
                    optionX + pillPadding, currentY + (pillHeight - fontSize) / 2f, 
                    textColor, fontSize);
            }
            
            currentX += pillWidth + 3f; // Уменьшил отступ с 4f до 3f
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;
        
        float currentX = 0;
        float currentY = y + HEADER_HEIGHT + PADDING;
        float contentWidth = width - PADDING * 2;
        float pillHeight = 12f; // Уменьшил с 16f
        float pillPadding = 6f; // Уменьшил с 8f
        float fontSize = 5.5f;
        
        for (BooleanSetting mode : multiSetting.get()) {
            float textWidth = Fonts.otwindowsa.getWidth(mode.getName(), fontSize);
            float pillWidth = textWidth + pillPadding * 2;
            
            // Если не влезает - переносим на новую строку
            if (currentX + pillWidth > contentWidth && currentX > 0) {
                currentX = 0;
                currentY += pillHeight + 2f; // Уменьшил с 3f
            }
            
            float optionX = x + PADDING + currentX;
            
            if (mouseX >= optionX && mouseX <= optionX + pillWidth &&
                mouseY >= currentY && mouseY <= currentY + pillHeight) {
                mode.set(!mode.get());
                return true;
            }
            
            currentX += pillWidth + 3f; // Уменьшил с 4f
        }
        return false;
    }
}


