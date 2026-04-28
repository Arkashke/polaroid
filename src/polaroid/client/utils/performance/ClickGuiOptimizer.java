package polaroid.client.utils.performance;

import java.util.HashMap;
import java.util.Map;

/**
 * Оптимизатор для ClickGUI
 */
public class ClickGuiOptimizer {
    
    private static final ClickGuiOptimizer INSTANCE = new ClickGuiOptimizer();
    
    // Кэш для анимаций модулей
    private final Map<String, Float> animationCache = new HashMap<>();
    
    // Счетчик кадров для обновления кэша
    private int frameCounter = 0;
    
    public static ClickGuiOptimizer getInstance() {
        return INSTANCE;
    }
    
    /**
     * Проверяет, нужно ли рендерить модуль (находится ли он в видимой области)
     */
    public boolean shouldRenderModule(float moduleY, float moduleHeight, float panelY, float panelHeight) {
        // Модуль полностью выше панели
        if (moduleY + moduleHeight < panelY) {
            return false;
        }
        
        // Модуль полностью ниже панели
        if (moduleY > panelY + panelHeight) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Кэширует значение анимации модуля
     */
    public void cacheAnimation(String moduleKey, float animationValue) {
        animationCache.put(moduleKey, animationValue);
    }
    
    /**
     * Получает закэшированное значение анимации
     */
    public Float getCachedAnimation(String moduleKey) {
        return animationCache.get(moduleKey);
    }
    
    /**
     * Вызывать каждый кадр
     */
    public void onFrame() {
        frameCounter++;
        
        // Очищаем кэш каждые 600 кадров (~10 секунд при 60 FPS)
        if (frameCounter >= 600) {
            frameCounter = 0;
            animationCache.clear();
        }
    }
    
    /**
     * Проверяет, нужно ли обновлять анимацию (throttling)
     */
    public boolean shouldUpdateAnimation(String moduleKey) {
        // Обновляем анимации каждый 2-й кадр для плавности
        return frameCounter % 2 == 0;
    }
}


