package polaroid.client.utils.performance;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Профайлер для отслеживания производительности модулей
 */
public class PerformanceProfiler {
    
    private static final PerformanceProfiler INSTANCE = new PerformanceProfiler();
    
    private final Map<String, ProfileData> profileMap = new ConcurrentHashMap<>();
    private boolean enabled = false;
    
    public static PerformanceProfiler getInstance() {
        return INSTANCE;
    }
    
    public void startProfiling(String moduleName) {
        if (!enabled) return;
        
        ProfileData data = profileMap.computeIfAbsent(moduleName, k -> new ProfileData());
        data.startTime = System.nanoTime();
    }
    
    public void endProfiling(String moduleName) {
        if (!enabled) return;
        
        ProfileData data = profileMap.get(moduleName);
        if (data != null && data.startTime > 0) {
            long elapsed = System.nanoTime() - data.startTime;
            data.totalTime += elapsed;
            data.callCount++;
            data.startTime = 0;
            
            // Обновляем максимальное время
            if (elapsed > data.maxTime) {
                data.maxTime = elapsed;
            }
        }
    }
    
    public Map<String, ProfileData> getProfileData() {
        return new HashMap<>(profileMap);
    }
    
    public void reset() {
        profileMap.clear();
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public static class ProfileData {
        public long totalTime = 0;
        public long callCount = 0;
        public long maxTime = 0;
        public long startTime = 0;
        
        public double getAverageTimeMs() {
            return callCount > 0 ? (totalTime / (double) callCount) / 1_000_000.0 : 0;
        }
        
        public double getMaxTimeMs() {
            return maxTime / 1_000_000.0;
        }
        
        public double getTotalTimeMs() {
            return totalTime / 1_000_000.0;
        }
    }
}


