package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.Polaroid;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;

/**
 * Панель категории с правильной layout системой
 */
public class CategoryPanel {
    
    private final Category category;
    private final ScrollContainer scrollContainer;
    private final VerticalContainer moduleContainer;
    
    private float x, y, width, height;
    
    public CategoryPanel(Category category, float x, float y, float width, float height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        // Создаем контейнер для модулей без padding
        moduleContainer = new VerticalContainer(3f);
        moduleContainer.setPadding(0f); // Убираем padding
        
        // Оборачиваем в scroll контейнер СНАЧАЛА
        scrollContainer = new ScrollContainer(moduleContainer);
        scrollContainer.setHeight(height - 22f); // Высота без заголовка
        
        // Теперь можно вызывать rebuildModules
        rebuildModules("");
    }
    
    /**
     * Перестраивает список модулей с учётом фильтра
     */
    public void rebuildModules(String searchQuery) {
        moduleContainer.clearChildren();
        
        String query = searchQuery.toLowerCase().trim();
        
        // Добавляем модули этой категории
        for (Module module : Polaroid.getInstance().getFunctionRegistry().getFunctions()) {
            if (module.getCategory() == category) {
                // Фильтрация по поисковому запросу
                if (query.isEmpty() || module.getName().toLowerCase().contains(query)) {
                    moduleContainer.addChild(new ModuleComponent(module));
                }
            }
        }
        
        scrollContainer.resetScroll();
    }
    
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        // ОПТИМИЗАЦИЯ: Обновляем оптимизатор каждый кадр
        polaroid.client.utils.performance.ClickGuiOptimizer.getInstance().onFrame();
        
        int activColor = Polaroid.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB();
        
        // ОПТИМИЗАЦИЯ: Кэшируем blur для панели (вызываем только раз в N кадров)
        // Фон панели категории (блюр удален, оптимальная прозрачность)
        DisplayUtils.drawRoundedRect(x, y, width, height, 
                new Vector4f(7.4f, 7.4f, 7.4f, 7.4f), ColorUtils.rgba(16, 16, 16, 185));
        
        DisplayUtils.drawRoundedRect(x, y, width, height, 
                new Vector4f(7.4f, 7.4f, 7.4f, 7.4f), ColorUtils.rgba(16, 16, 16, 150));
        
        // Заголовок (центрированный)
        String iconChar = category.getIcon();
        String categoryName = category.getName();
        
        // ОПТИМИЗАЦИЯ: Кэшируем ширину текста
        float iconWidth = polaroid.client.utils.performance.FontWidthCache.getInstance()
            .getWidth(iconChar, 10, (text, size) -> Fonts.excellenticon.getWidth(text, size));
        float textWidth = polaroid.client.utils.performance.FontWidthCache.getInstance()
            .getWidth(categoryName, 8, (text, size) -> Fonts.otwindowsa.getWidth(text, size));
        float totalWidth = iconWidth + 4 + textWidth;
        
        // Центрируем по панели
        float startX = x + (width - totalWidth) / 2f;
        
        Fonts.excellenticon.drawText(stack, iconChar, startX, y + 6, activColor, 10);
        Fonts.otwindowsa.drawText(stack, categoryName, startX + iconWidth + 4, y + 6f, 
                ColorUtils.rgba(255, 255, 255, 255), 8);
        
        // Measure -> Layout -> Render
        float contentWidth = width - 8;
        moduleContainer.measure(contentWidth);
        scrollContainer.layout(x + 4, y + 20, contentWidth);
        scrollContainer.render(stack, mouseX, mouseY);
    }
    
    public Module getHoveredModule(int mouseX, int mouseY) {
        // Проверяем наведение на модули
        for (UIComponent child : moduleContainer.getChildren()) {
            if (child instanceof ModuleComponent) {
                ModuleComponent mc = (ModuleComponent) child;
                // Проверяем если курсор над модулем
                if (mouseX >= mc.x && mouseX <= mc.x + mc.width && 
                    mouseY >= mc.y && mouseY <= mc.y + 18) {
                    return mc.getModule();
                }
            }
        }
        return null;
    }
    
    public void mouseClicked(double mouseX, double mouseY, int button) {
        scrollContainer.mouseClicked(mouseX, mouseY, button);
    }
    
    public void mouseReleased(double mouseX, double mouseY, int button) {
        scrollContainer.mouseReleased(mouseX, mouseY, button);
    }
    
    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollContainer.onScroll(mouseX, mouseY, delta);
    }
    
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        scrollContainer.keyPressed(keyCode, scanCode, modifiers);
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        scrollContainer.setHeight(height - 22f);
    }
    
    public void renderColorPickers(MatrixStack stack, int mouseX, int mouseY) {
        // Проходим по всем компонентам и рендерим открытые ColorPickerWindow
        for (UIComponent child : moduleContainer.getChildren()) {
            if (child instanceof ModuleComponent) {
                ModuleComponent moduleComponent = (ModuleComponent) child;
                moduleComponent.renderColorPickers(stack, mouseX, mouseY);
            }
        }
    }
    
    public boolean handleColorPickerClicks(double mouseX, double mouseY, int button) {
        // Проходим по всем компонентам и проверяем клики по ColorPickerWindow
        for (UIComponent child : moduleContainer.getChildren()) {
            if (child instanceof ModuleComponent) {
                ModuleComponent moduleComponent = (ModuleComponent) child;
                if (moduleComponent.handleColorPickerClicks(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void handleColorPickerReleased(double mouseX, double mouseY, int button) {
        // Проходим по всем компонентам и обрабатываем mouseReleased для ColorPickerWindow
        for (UIComponent child : moduleContainer.getChildren()) {
            if (child instanceof ModuleComponent) {
                ModuleComponent moduleComponent = (ModuleComponent) child;
                moduleComponent.handleColorPickerReleased(mouseX, mouseY, button);
            }
        }
    }
}


