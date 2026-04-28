package polaroid.client.modules.impl.misc;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.performance.PerformanceProfiler;
import polaroid.client.utils.performance.RenderCache;

@ModuleSystem(
    name = "PerformanceMode",
    type = Category.Misc,
    server = ServerCategory.NO,
    description = "Настройки производительности клиента"
)
public class PerformanceSettings extends Module {
    
    public final ModeSetting performanceMode = new ModeSetting(
        "Режим производительности",
        "Сбалансированный",
        "Максимальная производительность",
        "Сбалансированный",
        "Максимальное качество"
    );
    
    public final BooleanSetting enableProfiling = new BooleanSetting("Профилирование", false);
    
    public final BooleanSetting enableCaching = new BooleanSetting("Кэширование", true);
    
    public final SliderSetting cacheLifetime = new SliderSetting("Время жизни кэша (мс)", 50, 10, 200, 10)
        .setVisible(() -> enableCaching.get());
    
    public final BooleanSetting optimizeRendering = new BooleanSetting("Оптимизация рендеринга", true);
    
    public final BooleanSetting reduceParticles = new BooleanSetting("Уменьшить частицы", false);
    
    public final SliderSetting maxFPS = new SliderSetting("Макс. FPS", 0, 0, 300, 10);
    
    public PerformanceSettings() {
        addSettings(
            performanceMode,
            enableProfiling,
            enableCaching,
            cacheLifetime,
            optimizeRendering,
            reduceParticles,
            maxFPS
        );
    }
    
    @Override
    public boolean onEnable() {
        applySettings();
        return super.onEnable();
    }
    
    @Override
    public boolean onDisable() {
        resetSettings();
        return super.onDisable();
    }
    
    private void applySettings() {
        // Применяем профилирование
        PerformanceProfiler.getInstance().setEnabled(enableProfiling.get());
        
        // Применяем режим производительности
        switch (performanceMode.get()) {
            case "Максимальная производительность":
                applyMaxPerformance();
                break;
            case "Сбалансированный":
                applyBalanced();
                break;
            case "Максимальное качество":
                applyMaxQuality();
                break;
        }
        
        // Очищаем кэш при изменении настроек
        if (enableCaching.get()) {
            RenderCache.getInstance().invalidateAll();
        }
    }
    
    private void applyMaxPerformance() {
        // Настройки для максимальной производительности
        if (mc.gameSettings != null) {
            // Можно добавить изменение настроек Minecraft
            // mc.gameSettings.particles = ParticleStatus.MINIMAL;
        }
    }
    
    private void applyBalanced() {
        // Сбалансированные настройки
    }
    
    private void applyMaxQuality() {
        // Настройки для максимального качества
    }
    
    private void resetSettings() {
        PerformanceProfiler.getInstance().setEnabled(false);
        RenderCache.getInstance().invalidateAll();
    }
    
    public int getCacheLifetime() {
        return Math.round(cacheLifetime.get());
    }
    
    public boolean isCachingEnabled() {
        return enableCaching.get();
    }
    
    public boolean isRenderOptimizationEnabled() {
        return optimizeRendering.get();
    }
}


