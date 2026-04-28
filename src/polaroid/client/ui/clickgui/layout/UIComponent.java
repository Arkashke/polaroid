package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;

/**
 * Базовый класс для всех UI компонентов
 * Реализует трехфазную систему рендеринга: measure -> layout -> render
 */
public abstract class UIComponent {
    
    protected float x, y;
    protected float width, height;
    protected float measuredHeight;
    protected boolean needsLayout = true;
    
    /**
     * Фаза 1: Измерение
     * Компонент вычисляет свою высоту на основе контента
     */
    public abstract float measure(float availableWidth);
    
    /**
     * Фаза 2: Размещение
     * Компонент получает финальные координаты и размеры
     */
    public void layout(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = measuredHeight;
        this.needsLayout = false;
    }
    
    /**
     * Фаза 3: Рендеринг
     * Компонент отрисовывает себя
     */
    public abstract void render(MatrixStack stack, int mouseX, int mouseY);
    
    /**
     * Обработка клика мыши
     */
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    
    /**
     * Обработка отпускания мыши
     */
    public void mouseReleased(double mouseX, double mouseY, int button) {}
    
    /**
     * Обработка нажатия клавиши
     */
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
    
    /**
     * Проверка, находится ли курсор над компонентом
     */
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    /**
     * Получить измеренную высоту компонента
     */
    public float getMeasuredHeight() {
        return measuredHeight;
    }
    
    /**
     * Получить текущую высоту компонента
     */
    public float getHeight() {
        return height;
    }
    
    /**
     * Получить Y координату
     */
    public float getY() {
        return y;
    }
    
    /**
     * Пометить компонент для пересчета layout
     */
    public void invalidate() {
        needsLayout = true;
    }
}


