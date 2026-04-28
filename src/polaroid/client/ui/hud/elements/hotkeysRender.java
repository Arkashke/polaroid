package polaroid.client.ui.hud.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.utils.client.KeyStorage;
import polaroid.client.utils.drag.Dragging;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.math.Vector4i;
import polaroid.client.utils.render.*;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.text.GradientUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.ITextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class hotkeysRender implements hudRender {

    final Dragging dragging;

    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 7;
        float padding = 4;

        ITextComponent name = GradientUtil.gradient("HotKeys");
        
        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        InterFace interFace = moduleRegistry.getInterFace();
        
        // ОПТИМИЗАЦИЯ: Кэшируем цвета
        polaroid.client.utils.performance.ColorCache colorCache = polaroid.client.utils.performance.ColorCache.getInstance();
        colorCache.onFrame();
        
        int backgroundColor = colorCache.getColor("hud_bg", () -> interFace.getBackgroundColor());
        int borderColor = colorCache.getColor("hud_border", () -> interFace.getBorderColor());
        int textColor = colorCache.getColor("hud_text", () -> interFace.getTextColor());
        int separatorColor = colorCache.getColor("hud_separator", () -> interFace.getSeparatorColor());
        
        // ОПТИМИЗАЦИЯ: Убрали blur полностью - рисуем только фон
        DisplayUtils.drawRoundedRect(posX, posY, width, height, 6f, backgroundColor);
        
        // Обводка HUD (упрощенная без glow)
        if (interFace.hudOutline.get()) {
            int outlineColor = interFace.getHudOutlineColor(0);
            DisplayUtils.drawRoundedRectOutline(posX, posY, width, height, 6f, 1.2f, outlineColor);
        }
        
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        
        // Заголовок "KeyBinds"
        Fonts.otwindowsa.drawText(ms, "KeyBinds", posX + padding, posY - 1f + padding + 0.5f, textColor, 8);
        
        // Иконка клавиатуры справа
        Fonts.icons2.drawText(ms, "C", posX + width - padding - Fonts.icons2.getWidth("C", 10), posY - 1f + padding + 0.5f, textColor, 10, -0.1f);
        
        posY += fontSize + padding * 2;

        // ОПТИМИЗАЦИЯ: Кэшируем ширину заголовка
        float maxWidth = polaroid.client.utils.performance.FontWidthCache.getInstance()
            .getWidth("KeyBinds", fontSize, (text, size) -> Fonts.otwindowsa.getWidth(text, size)) + padding * 2;
        float localHeight = fontSize + padding * 2;

        // Проверяем есть ли активные бинды для отображения линии и получаем максимальное значение анимации
        float maxAnimationValue = 0;
        for (Module f : Polaroid.getInstance().getFunctionRegistry().getFunctions()) {
            if (f.getAnimation().getValue() > 0 && f.getBind() != 0) {
                maxAnimationValue = Math.max(maxAnimationValue, (float) f.getAnimation().getValue());
            }
        }
        
        // Рисуем разделительную линию с альфой зависящей от анимации последнего элемента
        if (maxAnimationValue > 0) {
            int lineAlpha = (int) (ColorUtils.getAlpha(separatorColor) * maxAnimationValue);
            int lineColorWithAlpha = ColorUtils.setAlpha(separatorColor, lineAlpha);
            DisplayUtils.drawRoundedRect(posX + padding, posY - 2f, width - padding * 2, 1, 
                    new net.minecraft.util.math.vector.Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 
                    lineColorWithAlpha);
        }

        posY += 3f;

        // Список модулей с биндами
        for (Module f : Polaroid.getInstance().getFunctionRegistry().getFunctions()) {
            // ОПТИМИЗАЦИЯ: Throttling обновления анимаций (каждый 2-й кадр)
            if (polaroid.client.utils.performance.ModuleThrottler.getInstance().shouldExecute("keybind_anim_" + f.getName(), 2)) {
                f.getAnimation().update();
            }
            
            if (!(f.getAnimation().getValue() > 0) || f.getBind() == 0) continue;
            
            String nameText = f.getName();
            // ОПТИМИЗАЦИЯ: Кэшируем ширину текста
            float nameWidth = polaroid.client.utils.performance.FontWidthCache.getInstance()
                .getWidth(nameText, fontSize, (text, size) -> Fonts.otwindowsa.getWidth(text, size));

            String bindText = KeyStorage.getKey(f.getBind());
            // Добавляем скобки если настройка включена
            if (interFace.brackets.get()) {
                bindText = "[" + bindText + "]";
            }
            float bindWidth = polaroid.client.utils.performance.FontWidthCache.getInstance()
                .getWidth(bindText, fontSize, (text, size) -> Fonts.otwindowsa.getWidth(text, size));

            float localWidth = nameWidth + bindWidth + padding * 3;

            // Название модуля слева
            Fonts.otwindowsa.drawText(ms, nameText, posX + padding, posY - 3, ColorUtils.setAlpha(textColor, (int)(255 * f.getAnimation().getValue())), fontSize);
            
            // Клавиша справа (того же цвета что и название)
            Fonts.otwindowsa.drawText(ms, bindText, posX + width - padding - bindWidth - 2.5f, posY - 3, ColorUtils.setAlpha(textColor, (int)(255 * f.getAnimation().getValue())), fontSize);
            
            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (float) ((fontSize + padding) * f.getAnimation().getValue());
            localHeight += (float) ((fontSize + padding) * f.getAnimation().getValue());
        }
        
        Scissor.unset();
        Scissor.pop();
        
        width = MathUtil.lerp(width, Math.max(maxWidth, 85.0f), 20.0f);
        height = MathUtil.lerp(height, Math.max(localHeight + 1, 10.0f), 20.0f);
        dragging.setWidth(width);
        dragging.setHeight(height);
    }
}


