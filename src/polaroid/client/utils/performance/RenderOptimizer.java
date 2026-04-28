package polaroid.client.utils.performance;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

/**
 * Оптимизатор рендеринга для улучшения FPS
 */
public class RenderOptimizer {
    
    private static final RenderOptimizer INSTANCE = new RenderOptimizer();
    private static final Minecraft mc = Minecraft.getInstance();
    
    private int frameCounter = 0;
    private long lastCleanupTime = 0;
    
    public static RenderOptimizer getInstance() {
        return INSTANCE;
    }
    
    /**
     * Вызывается каждый кадр для оптимизации
     */
    public void onRenderTick() {
        frameCounter++;
        
        // Очищаем кэши каждые 60 кадров (~1 секунда при 60 FPS)
        if (frameCounter >= 60) {
            frameCounter = 0;
            RenderCache.getInstance().cleanup();
        }
        
        // Очищаем старые данные каждые 5 секунд
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > 5000) {
            lastCleanupTime = currentTime;
            performCleanup();
        }
    }
    
    /**
     * Проверяет, находится ли объект в поле зрения
     */
    public boolean isInFrustum(double x, double y, double z, double radius) {
        if (mc.gameRenderer == null || mc.gameRenderer.getActiveRenderInfo() == null) {
            return true; // На всякий случай рендерим
        }
        
        // Упрощенная проверка frustum culling
        // Можно улучшить используя реальный frustum
        double dx = x - mc.gameRenderer.getActiveRenderInfo().getProjectedView().x;
        double dy = y - mc.gameRenderer.getActiveRenderInfo().getProjectedView().y;
        double dz = z - mc.gameRenderer.getActiveRenderInfo().getProjectedView().z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // Не рендерим объекты дальше 128 блоков
        return distance < 128.0 + radius;
    }
    
    /**
     * Батчинг для GL вызовов - начало батча
     */
    public void beginBatch() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
    
    /**
     * Батчинг для GL вызовов - конец батча
     */
    public void endBatch() {
        RenderSystem.disableBlend();
    }
    
    /**
     * Очистка ресурсов
     */
    private void performCleanup() {
        // Очищаем кэш рендеринга
        RenderCache.getInstance().cleanup();
        
        // Можно добавить другие очистки
    }
    
    /**
     * Оптимизированная проверка видимости для UI элементов
     */
    public boolean shouldRenderUI(int x, int y, int width, int height) {
        if (mc.getMainWindow() == null) return true;
        
        int screenWidth = mc.getMainWindow().getScaledWidth();
        int screenHeight = mc.getMainWindow().getScaledHeight();
        
        // Не рендерим если полностью за пределами экрана
        return !(x + width < 0 || x > screenWidth || y + height < 0 || y > screenHeight);
    }
}


