package polaroid.client.ui.hud.elements;

import polaroid.client.Polaroid;
import polaroid.client.modules.api.ModuleRegistry;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.events.EventDisplay;
import polaroid.client.utils.drag.Dragging;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.GaussianBlur;
import polaroid.client.utils.render.font.Fonts;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.vector.Vector4f;

@RequiredArgsConstructor
public class gpsCoordsRender implements hudRender {

    private final Dragging dragging;
    
    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        
        ModuleRegistry moduleRegistry = Polaroid.getInstance().getFunctionRegistry();
        InterFace interFace = moduleRegistry.getInterFace();
        
        // Проверяем настройку стиля координат
        if (interFace.coordsStyle != null && interFace.coordsStyle.is("Упрощенный")) {
            renderSimpleStyle(eventDisplay);
            return;
        }
        
        // Новый стиль
        renderModernStyle(ms, interFace);
    }
    
    private void renderSimpleStyle(EventDisplay eventDisplay) {
        float offset = 3;
        float fontSize = 7;
        float fontHeight = Fonts.otwindowsa.getHeight(fontSize);

        float posX = offset;
        float posY = window.getScaledHeight() - offset - fontHeight;

        float stringWidth = Fonts.otwindowsa.getWidth("XYZ: ", fontSize);

        Fonts.otwindowsa.drawTextWithOutline(eventDisplay.getMatrixStack(), "XYZ: ", posX, posY, -1, fontSize, 0.05f);

        Fonts.otwindowsa.drawTextWithOutline(eventDisplay.getMatrixStack(), (int) mc.player.getPosX() + ", "
                + (int) mc.player.getPosY() + ", " + (int) mc.player.getPosZ(), posX + stringWidth, posY, ColorUtils.rgb(158, 255, 185), fontSize, 0.05f);

        posY -= 12;
        stringWidth = Fonts.otwindowsa.getWidth("BPS: ", fontSize);

        Fonts.otwindowsa.drawTextWithOutline(eventDisplay.getMatrixStack(), "BPS: ", posX, posY, -1, fontSize, 0.05f);

        Fonts.otwindowsa.drawTextWithOutline(eventDisplay.getMatrixStack(), String.format("%.2f", Math.hypot(mc.player.prevPosX - mc.player.getPosX(), mc.player.prevPosZ - mc.player.getPosZ()) * 20), posX + stringWidth, posY, ColorUtils.rgb(158, 255, 185), fontSize, 0.05f);
    }
    
    private void renderModernStyle(MatrixStack ms, InterFace interFace) {
        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 7;
        float padding = 4;
        
        // Получаем цвета в зависимости от темы
        int backgroundColor = interFace.getBackgroundColor();
        int textColor = interFace.getTextColor();
        int separatorColor = interFace.getSeparatorColor();
        
        // Формируем текст
        String coords = (int) mc.player.getPosX() + ", " + (int) mc.player.getPosY() + ", " + (int) mc.player.getPosZ();
        String bps = String.format("%.2f", Math.hypot(mc.player.prevPosX - mc.player.getPosX(), mc.player.prevPosZ - mc.player.getPosZ()) * 20);
        
        // XYZ вместо иконки
        String xyzLabel = "XYZ";
        float xyzWidth = Fonts.otwindowsa.getWidth(xyzLabel, fontSize);
        
        // Вычисляем ширину
        float coordsWidth = Fonts.otwindowsa.getWidth(coords, fontSize);
        float bpsWidth = Fonts.otwindowsa.getWidth("BPS: " + bps, fontSize);
        float separatorWidth = Fonts.otwindowsa.getWidth(" | ", fontSize);
        
        float contentWidth = xyzWidth + separatorWidth + coordsWidth + 2 + separatorWidth + bpsWidth; // +2 для пробела после координат
        float totalWidth = contentWidth + padding * 2;
        float totalHeight = fontSize + padding * 2;
        
        // Тень
        DisplayUtils.drawShadow(posX, posY, totalWidth, totalHeight, 12, ColorUtils.rgba(0, 0, 0, 50));
        
        // Фон (блюр удален для оптимизации HUD)
        DisplayUtils.drawRoundedRect(posX, posY, totalWidth, totalHeight, 6f, backgroundColor);
        
        // Обводка HUD (оптимизировано - убран glow эффект)
        if (interFace.hudOutline.get()) {
            int outlineColor = interFace.getHudOutlineColor(0);
            DisplayUtils.drawRoundedRectOutline(posX, posY, totalWidth, totalHeight, 6f, 1.2f, outlineColor);
        }
        
        // Рендерим содержимое
        float currentX = posX + padding - 1;
        float textY = posY + padding;
        
        // XYZ (не сдвигаем)
        Fonts.otwindowsa.drawText(ms, xyzLabel, currentX + 1, textY, ColorUtils.setAlpha(textColor, 150), fontSize);
        currentX += xyzWidth + 2;
        
        // Разделитель
        Fonts.otwindowsa.drawText(ms, "|", currentX, textY, ColorUtils.setAlpha(textColor, 100), fontSize);
        currentX += separatorWidth;
        
        // Координаты (сдвинуты влево)
        Fonts.otwindowsa.drawText(ms, coords, currentX - 2, textY, textColor, fontSize);
        currentX += coordsWidth + 2; // +2 для пробела
        
        // Разделитель
        Fonts.otwindowsa.drawText(ms, "|", currentX - 2, textY, ColorUtils.setAlpha(textColor, 100), fontSize);
        currentX += separatorWidth;
        
        // BPS (сдвинуто влево)
        Fonts.otwindowsa.drawText(ms, "BPS: ", currentX - 2, textY, ColorUtils.setAlpha(textColor, 150), fontSize);
        currentX += Fonts.otwindowsa.getWidth("BPS: ", fontSize);
        Fonts.otwindowsa.drawText(ms, bps, currentX - 2, textY, textColor, fontSize);
        
        // Обновляем размеры для dragging
        width = MathUtil.lerp(width, totalWidth, 20.0f);
        height = MathUtil.lerp(height, totalHeight, 20.0f);
        dragging.setWidth(width);
        dragging.setHeight(height);
    }
}


