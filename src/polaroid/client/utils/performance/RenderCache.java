package polaroid.client.utils.performance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Кэш для рендеринга - позволяет переиспользовать результаты отрисовки
 */
public class RenderCache {
    
    private static final RenderCache INSTANCE = new RenderCache();
    
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    
    public static RenderCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Получает значение из кэша или вычисляет новое
     * @param key ключ кэша
     * @param ttlMs время жизни в миллисекундах
     * @param supplier функция для вычисления значения
     * @return закэшированное или новое значение
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCompute(String key, long ttlMs, Supplier<T> supplier) {
        CacheEntry<T> entry = (CacheEntry<T>) cache.get(key);
        long currentTime = System.currentTimeMillis();
        
        if (entry != null && (currentTime - entry.timestamp) < ttlMs) {
            return entry.value;
        }
        
        T newValue = supplier.get();
        cache.put(key, new CacheEntry<>(newValue, currentTime));
        return newValue;
    }
    
    public void invalidate(String key) {
        cache.remove(key);
    }
    
    public void invalidateAll() {
        cache.clear();
    }
    
    /**
     * Очищает устаревшие записи
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue().timestamp) > 60000 // 1 минута
        );
    }
    
    private static class CacheEntry<T> {
        final T value;
        final long timestamp;
        
        CacheEntry(T value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}


