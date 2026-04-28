package polaroid.client.utils.performance;

import polaroid.client.utils.render.Scissor;

/**
 * Оптимизатор Scissor - избегаем лишних push/pop
 */
public class ScissorOptimizer {
    
    private static final ScissorOptimizer INSTANCE = new ScissorOptimizer();
    private boolean isScissorActive = false;
    private float lastX, lastY, lastWidth, lastHeight;
    
    public static ScissorOptimizer getInstance() {
        return INSTANCE;
    }
    
    /**
     * Устанавливает scissor только если параметры изменились
     */
    public void setScissor(float x, float y, float width, float height) {
        if (!isScissorActive || 
            lastX != x || lastY != y || 
            lastWidth != width || lastHeight != height) {
            
            if (isScissorActive) {
                Scissor.unset();
                Scissor.pop();
            }
            
            Scissor.push();
            Scissor.setFromComponentCoordinates(x, y, width, height);
            
            lastX = x;
            lastY = y;
            lastWidth = width;
            lastHeight = height;
            isScissorActive = true;
        }
    }
    
    /**
     * Отключает scissor
     */
    public void disable() {
        if (isScissorActive) {
            Scissor.unset();
            Scissor.pop();
            isScissorActive = false;
        }
    }
    
    /**
     * Проверяет, активен ли scissor
     */
    public boolean isActive() {
        return isScissorActive;
    }
}


