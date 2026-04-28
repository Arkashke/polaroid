package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;
import polaroid.client.utils.render.Scissor;

/**
 * Контейнер со скроллингом
 * Оборачивает другой компонент и добавляет возможность прокрутки
 */
public class ScrollContainer extends UIComponent {
    
    private UIComponent content;
    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;
    private float scrollSpeed = 15f;
    
    public ScrollContainer(UIComponent content) {
        this.content = content;
    }
    
    public void setContent(UIComponent content) {
        this.content = content;
        invalidate();
    }
    
    @Override
    public float measure(float availableWidth) {
        if (content != null) {
            content.measure(availableWidth);
        }
        // ScrollContainer сам определяет свою высоту
        // Контент может быть больше
        measuredHeight = height; // Используем заданную высоту
        return measuredHeight;
    }
    
    @Override
    public void layout(float x, float y, float width) {
        super.layout(x, y, width);
        
        if (content != null) {
            // Контент размещается с учетом скролла
            content.layout(x, y + scrollOffset, width);
        }
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (content == null) return;
        
        // Плавная анимация скролла
        scrollOffset = polaroid.client.utils.math.MathUtil.fast(scrollOffset, targetScrollOffset, scrollSpeed);
        
        // Пересчитываем layout с новым offset
        content.layout(x, y + scrollOffset, width);
        
        // Scissor для обрезки контента (расширяем область на 2 пикселя для обводки)
        Scissor.push();
        Scissor.setFromComponentCoordinates(x - 2, y - 2, width + 4, height + 2);
        
        content.render(stack, mouseX, mouseY);
        
        Scissor.unset();
        Scissor.pop();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return false;
        
        if (content != null) {
            return content.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
    
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (content != null) {
            content.mouseReleased(mouseX, mouseY, button);
        }
    }
    
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (content != null) {
            content.keyPressed(keyCode, scanCode, modifiers);
        }
    }
    
    /**
     * Обработка скролла колесиком мыши
     */
    public void onScroll(double mouseX, double mouseY, double delta) {
        if (!isHovered(mouseX, mouseY)) return;
        
        if (content != null) {
            float contentHeight = content.getMeasuredHeight();
            float maxScroll = Math.max(0, contentHeight - height);
            
            targetScrollOffset += (float) (delta * 25.0);
            targetScrollOffset = MathHelper.clamp(targetScrollOffset, -maxScroll, 0);
        }
    }
    
    /**
     * Получить текущий offset скролла
     */
    public float getScrollOffset() {
        return scrollOffset;
    }
    
    /**
     * Установить высоту контейнера
     */
    public void setHeight(float height) {
        this.height = height;
        this.measuredHeight = height;
    }
    
    /**
     * Сбросить скролл в начало
     */
    public void resetScroll() {
        scrollOffset = 0;
        targetScrollOffset = 0;
    }
}


