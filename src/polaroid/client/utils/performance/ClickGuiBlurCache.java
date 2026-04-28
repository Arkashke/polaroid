package polaroid.client.utils.performance;

import java.util.HashMap;
import java.util.Map;

/**
 * Кэш для blur эффектов в ClickGUI
 * Вместо рендеринга blur каждый кадр - рендерим раз в N кадров
 */
public class ClickGuiBlurCache {
    
    private static final ClickGuiBlurCache INSTANCE = new ClickGuiBlurCache();
    
    // Интервал обновления blur (в кадрах) - максимально оптимизировано
    private static final int BLUR_UPDATE_INTERVAL = 8; // Обновляем каждый 8-й кадр (было 5)
    
    private final Map<String, Integer> lastBlurFrame = new HashMap<>();
    private int currentFrame = 0;
    
    public static ClickGuiBlurCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Проверяет, нужно ли рендерить blur для данного элемента
     */
    public boolean shouldRenderBlur(String elementKey) {
        currentFrame++;
        
        Integer lastFrame = lastBlurFrame.get(elementKey);
        
        // Если blur еще не рендерился или прошло достаточно кадров
        if (lastFrame == null || (currentFrame - lastFrame) >= BLUR_UPDATE_INTERVAL) {
            lastBlurFrame.put(elementKey, currentFrame);
            return true;
        }
        
        return false;
    }
    
    /**
     * Сбрасывает кэш (вызывать при закрытии GUI)
     */
    public void reset() {
        lastBlurFrame.clear();
        currentFrame = 0;
    }
    
    /**
     * Устанавливает интервал обновления blur
     * @param interval количество кадров между обновлениями (1 = каждый кадр, 3 = каждый 3-й кадр)
     */
    public void setUpdateInterval(int interval) {
        // Можно добавить настройку если нужно
    }
}


