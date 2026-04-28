package polaroid.client.utils.performance;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.GaussianBlur;

import java.util.ArrayList;
import java.util.List;

/**
 * Батчинг рендеринга для HUD элементов
 * Вместо отдельного blur для каждого элемента - один blur для всех
 */
public class HudBatchRenderer {
    
    private static final HudBatchRenderer INSTANCE = new HudBatchRenderer();
    
    private final List<HudElement> elementsToRender = new ArrayList<>();
    private boolean isBatching = false;
    
    public static HudBatchRenderer getInstance() {
        return INSTANCE;
    }
    
    /**
     * Начать батчинг HUD элементов
     */
    public void beginBatch() {
        elementsToRender.clear();
        isBatching = true;
    }
    
    /**
     * Добавить элемент в батч
     */
    public void addElement(float x, float y, float width, float height, float radius, int backgroundColor) {
        if (isBatching) {
            elementsToRender.add(new HudElement(x, y, width, height, radius, backgroundColor));
        }
    }
    
    /**
     * Завершить батчинг и отрендерить все элементы
     */
    public void endBatch() {
        if (!isBatching || elementsToRender.isEmpty()) {
            isBatching = false;
            return;
        }
        
        // Рисуем тени для всех элементов
        for (HudElement element : elementsToRender) {
            DisplayUtils.drawShadow(element.x, element.y, element.width, element.height, 12, 
                ColorUtils.rgba(0, 0, 0, 50));
        }
        
        // ОДИН blur для всех элементов
        GaussianBlur.startBlur();
        for (HudElement element : elementsToRender) {
            DisplayUtils.drawRoundedRect(element.x, element.y, element.width, element.height, 
                element.radius, element.backgroundColor);
        }
        GaussianBlur.endBlur(15, 3);
        
        // Рисуем фоны
        for (HudElement element : elementsToRender) {
            DisplayUtils.drawRoundedRect(element.x, element.y, element.width, element.height, 
                element.radius, element.backgroundColor);
        }
        
        elementsToRender.clear();
        isBatching = false;
    }
    
    public boolean isBatching() {
        return isBatching;
    }
    
    private static class HudElement {
        final float x, y, width, height, radius;
        final int backgroundColor;
        
        HudElement(float x, float y, float width, float height, float radius, int backgroundColor) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.radius = radius;
            this.backgroundColor = backgroundColor;
        }
    }
}


