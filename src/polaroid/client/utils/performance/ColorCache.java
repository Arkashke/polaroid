package polaroid.client.utils.performance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кэш для цветов - избегаем повторных вычислений RGBA
 */
public class ColorCache {
    
    private static final ColorCache INSTANCE = new ColorCache();
    private final Map<String, Integer> cache = new ConcurrentHashMap<>();
    private int frameCounter = 0;
    
    public static ColorCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Получить цвет с кэшированием
     */
    public int getColor(String key, ColorSupplier supplier) {
        Integer cached = cache.get(key);
        
        if (cached != null) {
            return cached;
        }
        
        int color = supplier.get();
        cache.put(key, color);
        return color;
    }
    
    /**
     * Вызывать каждый кадр для очистки старого кэша
     */
    public void onFrame() {
        frameCounter++;
        // Очищаем кэш каждые 100 кадров (~1.5 секунды при 60 FPS)
        if (frameCounter >= 100) {
            frameCounter = 0;
            cache.clear();
        }
    }
    
    @FunctionalInterface
    public interface ColorSupplier {
        int get();
    }
}


