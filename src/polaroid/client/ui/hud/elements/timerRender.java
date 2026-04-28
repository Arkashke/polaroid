package polaroid.client.ui.hud.elements;

import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.movement.Timer;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.utils.drag.Dragging;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.GaussianBlur;
import polaroid.client.utils.render.Scissor;
import polaroid.client.utils.render.font.Fonts;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class timerRender implements hudRender {

    final Dragging dragging;

    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        Timer timer = moduleRegistry.getTimer();

        // Если модуль выключен, не рисуем
        if (timer == null || !timer.isState()) {
            return;
        }

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 7;
        float padding = 4;

        InterFace interFace = moduleRegistry.getInterFace();

        // Получаем цвета в зависимости от темы
        int backgroundColor = interFace.getBackgroundColor();
        int textColor = interFace.getTextColor();
        int separatorColor = interFace.getSeparatorColor();

        // Вычисляем процент заполнения (0-100%)
        float violationPercent = (timer.getViolation() / timer.maxViolation) * 100.0f;
        violationPercent = Math.max(0, Math.min(100, violationPercent));

        // Размеры
        float barWidth = 80;
        float barHeight = 4;
        float percentTextHeight = fontSize - 1;
        float totalHeight = fontSize + padding * 2 + percentTextHeight + 1 + barHeight + padding;

        width = barWidth + padding * 2;
        height = totalHeight;

        // Тень вокруг всего контейнера
        DisplayUtils.drawShadow(posX, posY, width, height, 12, ColorUtils.rgba(0, 0, 0, 50));

        // Основной полупрозрачный фон контейнера (блюр удален для оптимизации HUD)
        DisplayUtils.drawRoundedRect(posX, posY, width, height, 6f, backgroundColor);

        // Обводка HUD (оптимизировано - убран glow эффект)
        if (interFace.hudOutline.get()) {
            int outlineColor = interFace.getHudOutlineColor(0);
            DisplayUtils.drawRoundedRectOutline(posX, posY, width, height, 6f, 1.2f, outlineColor);
        }

        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);

        // Заголовок "Timer" по центру
        String timerText = "Timer";
        float timerTextWidth = Fonts.otwindowsa.getWidth(timerText, 8);
        Fonts.otwindowsa.drawText(ms, timerText, posX + (width - timerTextWidth) / 2, posY - 1f + padding + 0.5f, textColor, 8);

        float currentY = posY + fontSize + padding * 2;
        
        // Рисуем разделительную линию (без анимации для этого элемента)
        DisplayUtils.drawRoundedRect(posX + padding, currentY - 2f, width - padding * 2, 1, 
                new net.minecraft.util.math.vector.Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 
                separatorColor);
        
        currentY += 1.5f;

        // Текст процента над полоской (прямо над ней)
        String percentText = String.format("%.0f%%", violationPercent);
        
        // Добавляем скобки если включена настройка
        if (interFace.brackets.get()) {
            percentText = "[" + percentText + "]";
        }
        
        float percentWidth = Fonts.otwindowsa.getWidth(percentText, fontSize - 1);
        Fonts.otwindowsa.drawText(ms, percentText,
                posX + padding + (barWidth - percentWidth) / 2,
                currentY,
                textColor,
                fontSize - 1);

        currentY += percentTextHeight + 1;

        float barY = currentY;

        // Заполненная часть полоски (градиент от зеленого к красному)
        if (violationPercent > 0) {
            float filledWidth = (barWidth * violationPercent) / 100.0f;

            // Цвет в зависимости от процента: зеленый (0%) -> желтый (50%) -> красный (100%)
            int barColor;
            if (violationPercent < 50) {
                // От зеленого к желтому
                float t = violationPercent / 50.0f;
                barColor = ColorUtils.interpolateColor(
                        ColorUtils.rgb(50, 205, 50),  // Зеленый
                        ColorUtils.rgb(255, 215, 0),  // Желтый
                        t
                );
            } else {
                // От желтого к красному
                float t = (violationPercent - 50) / 50.0f;
                barColor = ColorUtils.interpolateColor(
                        ColorUtils.rgb(255, 215, 0),  // Желтый
                        ColorUtils.rgb(255, 69, 58),  // Красный
                        t
                );
            }

            DisplayUtils.drawRoundedRect(posX + padding, barY, filledWidth, barHeight,
                    new net.minecraft.util.math.vector.Vector4f(2f, 2f, 2f, 2f),
                    barColor);
        }

        Scissor.unset();
        Scissor.pop();

        dragging.setWidth(width);
        dragging.setHeight(height);
    }
}


