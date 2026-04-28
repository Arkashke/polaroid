package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;

/**
 * Вертикальный контейнер
 * Автоматически размещает дочерние элементы один под другим
 */
public class VerticalContainer extends UIComponent {
    
    private final List<UIComponent> children = new ArrayList<>();
    private float spacing = 3f; // Отступ между элементами
    private float padding = 0f; // Внутренний отступ
    
    public VerticalContainer() {}
    
    public VerticalContainer(float spacing) {
        this.spacing = spacing;
    }
    
    public void addChild(UIComponent child) {
        children.add(child);
        invalidate();
    }
    
    public void removeChild(UIComponent child) {
        children.remove(child);
        invalidate();
    }
    
    public void clearChildren() {
        children.clear();
        invalidate();
    }
    
    public List<UIComponent> getChildren() {
        return children;
    }
    
    public void setSpacing(float spacing) {
        this.spacing = spacing;
        invalidate();
    }
    
    public void setPadding(float padding) {
        this.padding = padding;
        invalidate();
    }
    
    @Override
    public float measure(float availableWidth) {
        float totalHeight = padding * 2;
        float contentWidth = availableWidth - padding * 2;
        
        for (UIComponent child : children) {
            float childHeight = child.measure(contentWidth);
            totalHeight += childHeight;
            if (child != children.get(children.size() - 1)) {
                totalHeight += spacing;
            }
        }
        
        measuredHeight = totalHeight;
        return measuredHeight;
    }
    
    @Override
    public void layout(float x, float y, float width) {
        super.layout(x, y, width);
        
        float currentY = y + padding;
        float contentWidth = width - padding * 2;
        float contentX = x + padding;
        
        for (UIComponent child : children) {
            child.layout(contentX, currentY, contentWidth);
            currentY += child.getHeight() + spacing;
        }
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        for (UIComponent child : children) {
            child.render(stack, mouseX, mouseY);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (UIComponent child : children) {
            if (child.isHovered(mouseX, mouseY)) {
                if (child.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (UIComponent child : children) {
            child.mouseReleased(mouseX, mouseY, button);
        }
    }
    
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (UIComponent child : children) {
            child.keyPressed(keyCode, scanCode, modifiers);
        }
    }
    
    /**
     * Получить общую высоту контента (для скроллинга)
     */
    public float getContentHeight() {
        return measuredHeight;
    }
}


