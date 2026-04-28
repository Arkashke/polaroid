package polaroid.client.utils.performance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кэш для ширины текста - избегаем повторных вычислений
 */
public class FontWidthCache {
    
    private static final FontWidthCache INSTANCE = new FontWidthCache();
    private final Map<String, Float> cache = new ConcurrentHashMap<>();
    private int frameCounter = 0;
    
    public static FontWidthCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Получить ширину текста с кэшированием
     */
    public float getWidth(String text, float fontSize, WidthCalculator calculator) {
        String key = text + "_" + fontSize;
        Float cached = cache.get(key);
        
        if (cached != null) {
            return cached;
        }
        
        float width = calculator.calculate(text, fontSize);
        cache.put(key, width);
        return width;
    }
    
    /**
     * Вызывать каждый кадр для очистки старого кэша
     */
    public void onFrame() {
        frameCounter++;
        // Очищаем кэш каждые 300 кадров (~5 секунд при 60 FPS)
        if (frameCounter >= 300) {
            frameCounter = 0;
            cache.clear();
        }
    }
    
    @FunctionalInterface
    public interface WidthCalculator {
        float calculate(String text, float fontSize);
    }
}


