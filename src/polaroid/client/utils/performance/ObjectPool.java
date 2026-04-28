package polaroid.client.utils.performance;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Пул объектов для переиспользования и уменьшения нагрузки на GC
 */
public class ObjectPool<T> {
    
    private final Queue<T> pool = new ConcurrentLinkedQueue<>();
    private final Supplier<T> factory;
    private final int maxSize;
    
    public ObjectPool(Supplier<T> factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
    }
    
    /**
     * Получает объект из пула или создает новый
     */
    public T acquire() {
        T obj = pool.poll();
        return obj != null ? obj : factory.get();
    }
    
    /**
     * Возвращает объект в пул
     */
    public void release(T obj) {
        if (obj != null && pool.size() < maxSize) {
            pool.offer(obj);
        }
    }
    
    /**
     * Очищает пул
     */
    public void clear() {
        pool.clear();
    }
    
    public int size() {
        return pool.size();
    }
}


