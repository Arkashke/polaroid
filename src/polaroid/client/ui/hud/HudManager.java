package polaroid.client.ui.hud;

import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.events.EventDisplay;
import polaroid.client.ui.hud.elements.*;
import polaroid.client.utils.performance.HudBatchRenderer;
import polaroid.client.utils.performance.FontWidthCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер HUD элементов с оптимизированным рендерингом
 */
public class HudManager {
    
    private static final HudManager INSTANCE = new HudManager();
    private final List<hudRender> hudElements = new ArrayList<>();
    
    public static HudManager getInstance() {
        return INSTANCE;
    }
    
    public void addElement(hudRender element) {
        hudElements.add(element);
    }
    
    public void removeElement(hudRender element) {
        hudElements.remove(element);
    }
    
    /**
     * Оптимизированный рендеринг всех HUD элементов
     * ПРИМЕЧАНИЕ: Батчинг больше не используется после удаления blur из HUD
     */
    public void renderAll(EventDisplay eventDisplay) {
        // Очищаем кэш ширины текста каждый кадр
        FontWidthCache.getInstance().onFrame();
        
        // Рендерим все элементы напрямую (без батчинга, т.к. blur удален)
        for (hudRender element : hudElements) {
            try {
                element.render(eventDisplay);
            } catch (Exception e) {
                // Игнорируем ошибки отдельных элементов
            }
        }
    }
}


