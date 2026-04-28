package polaroid.client.utils.performance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Система throttling для модулей - позволяет выполнять модули реже чем каждый тик
 */
public class ModuleThrottler {
    
    private static final ModuleThrottler INSTANCE = new ModuleThrottler();
    
    private final Map<String, ThrottleData> throttleMap = new ConcurrentHashMap<>();
    
    public static ModuleThrottler getInstance() {
        return INSTANCE;
    }
    
    /**
     * Проверяет, должен ли модуль выполниться в этом тике
     * @param moduleName имя модуля
     * @param tickInterval интервал в тиках (1 = каждый тик, 2 = каждый второй тик, и т.д.)
     * @return true если модуль должен выполниться
     */
    public boolean shouldExecute(String moduleName, int tickInterval) {
        if (tickInterval <= 1) return true;
        
        ThrottleData data = throttleMap.computeIfAbsent(moduleName, k -> new ThrottleData());
        data.tickCounter++;
        
        if (data.tickCounter >= tickInterval) {
            data.tickCounter = 0;
            return true;
        }
        
        return false;
    }
    
    public void reset(String moduleName) {
        throttleMap.remove(moduleName);
    }
    
    public void resetAll() {
        throttleMap.clear();
    }
    
    private static class ThrottleData {
        int tickCounter = 0;
    }
}


