package polaroid.client.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.ui.clickgui.layout.CategoryPanel;
import polaroid.client.ui.clickgui.layout.SearchComponent;
import polaroid.client.config.Config;
import polaroid.client.config.ConfigStorage;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.font.Fonts;
import ru.hogoshi.Animation;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static polaroid.client.utils.client.IMinecraft.mc;

public class Window extends Screen {

    private Vector2f position = new Vector2f(0, 0);
    private List<CategoryPanel> panels = new ArrayList<>();
    public Animation animation = new Animation();
    private SearchComponent searchComponent;
    private String lastSearchQuery = "";
    private float lastScale = 1.0f;
    private int lastScreenWidth = 0;
    private int lastScreenHeight = 0;

    // Конфиг‑панель
    private boolean configsOpen = false;
    private float configsHover = 0f;
    private static final float CONFIGS_ANIM_SPEED = 0.2f;
    private float configsPanelHeight = 120f;
    private final ConfigStorage configStorage = polaroid.client.Polaroid.getInstance().getConfigStorage();
    private String activeConfigName = Config.currentConfig;
    
    private int[] transformMouseCoords(double mouseX, double mouseY) {
        double currentGuiScale = mc.getMainWindow().getGuiScaleFactor();
        double targetGuiScale = 2.0;
        double scaleFactor = currentGuiScale / targetGuiScale;
        
        return new int[] {
            (int)(mouseX * scaleFactor),
            (int)(mouseY * scaleFactor)
        };
    }

    public Window(ITextComponent titleIn) {
        super(titleIn);
        int width = mc.getMainWindow().getFramebufferWidth();
        int height = mc.getMainWindow().getFramebufferHeight();
        lastScreenWidth = (int)((double)width / 2.0);
        lastScreenHeight = (int)((double)height / 2.0);
        initPanels();
    }
    
    private void initPanels() {
        panels.clear();
        
        polaroid.client.modules.api.ModuleRegistry moduleRegistry = polaroid.client.Polaroid.getInstance().getFunctionRegistry();
        polaroid.client.modules.impl.render.ClickGui clickGui = moduleRegistry.getClickGui();
        float uiScale = clickGui != null ? clickGui.scale.get() : 1.0f;
        
        int width = mc.getMainWindow().getFramebufferWidth();
        int height = mc.getMainWindow().getFramebufferHeight();
        float screenWidth = (float)((double)width / 2.0);
        float screenHeight = (float)((double)height / 2.0);
        
        float panelWidth = 120 * uiScale;
        float spacing = 7 * uiScale;
        float panelHeight = 300 * uiScale;
        
        float totalWidth = (Category.values().length * panelWidth) + ((Category.values().length - 1) * spacing);
        
        if (totalWidth > screenWidth - 40) {
            float scale = (screenWidth - 40) / totalWidth;
            panelWidth *= scale;
            spacing *= scale;
        }
        
        if (panelHeight > screenHeight - 120) {
            panelHeight = screenHeight - 120;
        }
        
        totalWidth = (Category.values().length * panelWidth) + ((Category.values().length - 1) * spacing);
        float startX = (screenWidth - totalWidth) / 2f;
        float startY = (screenHeight - panelHeight) / 2f;
        
        float currentX = startX;
        for (Category category : Category.values()) {
            CategoryPanel panel = new CategoryPanel(category, currentX, startY, panelWidth, panelHeight);
            panels.add(panel);
            currentX += panelWidth + spacing;
        }
        
        float searchWidth = panelWidth;
        float searchX = screenWidth / 2f - searchWidth / 2f;
        float searchY = startY + panelHeight + 10 * uiScale;
        searchComponent = new SearchComponent(searchX, searchY, searchWidth);
    }

    @Override
    protected void init() {
        super.init();
        initPanels();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        GL11.glPushMatrix();
        mc.gameRenderer.setupOverlayRendering(2);
        
        // Трансформируем координаты мыши под GuiScale 2
        double currentGuiScale = mc.getMainWindow().getGuiScaleFactor();
        double targetGuiScale = 2.0;
        double scaleFactor = currentGuiScale / targetGuiScale;
        
        int transformedMouseX = (int)(mouseX * scaleFactor);
        int transformedMouseY = (int)(mouseY * scaleFactor);

        animation.update();
        
        // Проверяем изменение масштаба
        polaroid.client.modules.api.ModuleRegistry moduleRegistry = polaroid.client.Polaroid.getInstance().getFunctionRegistry();
        polaroid.client.modules.impl.render.ClickGui clickGui = moduleRegistry.getClickGui();
        float currentScale = clickGui != null ? clickGui.scale.get() : 1.0f;
        
        // Получаем размеры экрана для GuiScale 2 - вычисляем вручную
        int width = mc.getMainWindow().getFramebufferWidth();
        int height = mc.getMainWindow().getFramebufferHeight();
        int currentScreenWidth = (int)((double)width / 2.0);
        int currentScreenHeight = (int)((double)height / 2.0);
        
        // Пересоздаем панели если изменился масштаб ClickGui или размер экрана
        if (Math.abs(currentScale - lastScale) > 0.001f || 
            currentScreenWidth != lastScreenWidth || 
            currentScreenHeight != lastScreenHeight) {
            lastScale = currentScale;
            lastScreenWidth = currentScreenWidth;
            lastScreenHeight = currentScreenHeight;
            initPanels(); // Пересоздаем панели с новым масштабом
        }
        
        // Обновляем фильтрацию если поисковый запрос изменился
        String currentQuery = searchComponent.getSearchText();
        if (!currentQuery.equals(lastSearchQuery)) {
            for (CategoryPanel panel : panels) {
                panel.rebuildModules(currentQuery);
            }
            lastSearchQuery = currentQuery;
        }

        // Используем трансформированные координаты мыши
        for (CategoryPanel panel : panels) {
            panel.render(matrixStack, transformedMouseX, transformedMouseY);
        }
        
        // Рендерим поле поиска
        searchComponent.render(matrixStack, transformedMouseX, transformedMouseY);

        // Рендерим кнопку и панель конфигов
        renderConfigsSection(matrixStack, transformedMouseX, transformedMouseY);
        
        // Рендерим все открытые ColorPickerWindow поверх всего
        renderColorPickers(matrixStack, transformedMouseX, transformedMouseY);
        
        // Отображение описания модуля статично в верхней части экрана
        Module hoveredModule = getHoveredModule(transformedMouseX, transformedMouseY);
        if (hoveredModule != null) {
            String description = hoveredModule.getDescription();
            if (description != null && !description.isEmpty()) {
                // Вычисляем ширину экрана для GuiScale 2
                int fbWidth = mc.getMainWindow().getFramebufferWidth();
                float screenWidth = (float)((double)fbWidth / 2.0);
                float descX = screenWidth / 2f;
                float descY = 35; // Статичная позиция сверху (пониже)
                
                // Текст с черной обводкой (рисуем черный текст со смещением, потом белый)
                Fonts.otwindowsa.drawCenteredText(matrixStack, description, descX + 0.5f, descY + 0.5f, 
                        ColorUtils.rgba(0, 0, 0, 200), 12);
                Fonts.otwindowsa.drawCenteredText(matrixStack, description, descX, descY, 
                        ColorUtils.rgba(255, 255, 255, 255), 12);
            }
        }
        
        String egg = "Gupka лох";
        float eggSize = 4.5f;
        int eggColor = ColorUtils.rgba(120, 120, 120, 35);
        float eggX = currentScreenWidth - Fonts.otwindowsa.getWidth(egg, eggSize) - 4;
        float eggY = currentScreenHeight - 12;
        Fonts.otwindowsa.drawText(matrixStack, egg, eggX, eggY, eggColor, eggSize);
        
        GL11.glPopMatrix();
    }
    
    private void renderConfigsSection(MatrixStack matrixStack, int mouseX, int mouseY) {
        // Позиционируем кнопку \"Конфиги\" под полем поиска
        int fbWidth = mc.getMainWindow().getFramebufferWidth();
        int fbHeight = mc.getMainWindow().getFramebufferHeight();
        float screenWidth = (float)((double)fbWidth / 2.0);
        float screenHeight = (float)((double)fbHeight / 2.0);

        float buttonWidth = 90f;
        float buttonHeight = 14f;
        float buttonX = screenWidth - buttonWidth - 8f;
        float buttonY = screenHeight - buttonHeight - 8f;

        boolean hovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth
                && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
        configsHover = hovered ? Math.min(1f, configsHover + CONFIGS_ANIM_SPEED)
                               : Math.max(0f, configsHover - CONFIGS_ANIM_SPEED);

        int bg = ColorUtils.rgba(20, 20, 30, 120 + (int)(80 * configsHover));
        int border = ColorUtils.rgba(120, 120, 200, 120 + (int)(100 * configsHover));
        int textColor = ColorUtils.rgba(230, 230, 230, 255);

        Fonts.otwindowsa.drawCenteredText(matrixStack, "", 0, 0, 0, 0); // прогрев шрифта

        polaroid.client.utils.render.DisplayUtils.drawRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight,
                new net.minecraft.util.math.vector.Vector4f(3, 3, 3, 3), bg);
        polaroid.client.utils.render.DisplayUtils.drawRoundedRectOutline(buttonX, buttonY, buttonWidth, buttonHeight,
                3, 0.8f, border);

        String caption = configsOpen ? "Конфиги ▲" : "Конфиги ▼";
        Fonts.otwindowsa.drawCenteredText(matrixStack, caption, buttonX + buttonWidth / 2f,
                buttonY + buttonHeight / 2f - 3f, textColor, 7.5f);

        if (!configsOpen) {
            return;
        }

        float panelWidth = 190f;
        float panelX = screenWidth - panelWidth - 8f;
        float panelY = buttonY - configsPanelHeight - 6f;

        int panelBg = ColorUtils.rgba(10, 10, 20, 200);
        int panelBorder = ColorUtils.rgba(140, 140, 210, 180);

        polaroid.client.utils.render.DisplayUtils.drawRoundedRect(panelX, panelY, panelWidth, configsPanelHeight,
                new net.minecraft.util.math.vector.Vector4f(4, 4, 4, 4), panelBg);
        polaroid.client.utils.render.DisplayUtils.drawRoundedRectOutline(panelX, panelY, panelWidth, configsPanelHeight,
                4, 1f, panelBorder);

        float titleY = panelY + 6f;
        Fonts.otwindowsa.drawCenteredText(matrixStack, "Конфигурации", panelX + panelWidth / 2f,
                titleY, ColorUtils.rgba(255, 255, 255, 230), 8.5f);

        // Список конфигов
        float listTop = titleY + 10f;
        float itemHeight = 11f;
        float paddingX = 6f;
        java.util.List<Config> configs = configStorage.getConfigs();

        float y = listTop;
        for (Config cfg : configs) {
            String name = cfg.getName();
            boolean active = name.equalsIgnoreCase(activeConfigName);
            int nameColor = active ? ColorUtils.rgba(200, 255, 200, 255) : ColorUtils.rgba(220, 220, 220, 255);
            Fonts.otwindowsa.drawText(matrixStack, name, panelX + paddingX, y, nameColor, 7.5f);
            y += itemHeight;
            if (y > panelY + configsPanelHeight - 28f) {
                break;
            }
        }

        // Кнопки действий: Load / Save / Create / Delete
        float buttonsY = panelY + configsPanelHeight - 18f;
        float actionWidth = (panelWidth - paddingX * 2 - 6f) / 4f;
        float bx = panelX + paddingX;
        drawConfigActionButton(matrixStack, "Load", bx, buttonsY, actionWidth, itemHeight);
        bx += actionWidth + 2f;
        drawConfigActionButton(matrixStack, "Save", bx, buttonsY, actionWidth, itemHeight);
        bx += actionWidth + 2f;
        drawConfigActionButton(matrixStack, "Create", bx, buttonsY, actionWidth, itemHeight);
        bx += actionWidth + 2f;
        drawConfigActionButton(matrixStack, "Del", bx, buttonsY, actionWidth, itemHeight);
    }

    private void drawConfigActionButton(MatrixStack matrixStack, String text, float x, float y, float w, float h) {
        int bg = ColorUtils.rgba(30, 30, 45, 180);
        int border = ColorUtils.rgba(120, 120, 160, 200);
        int tc = ColorUtils.rgba(230, 230, 230, 255);
        polaroid.client.utils.render.DisplayUtils.drawRoundedRect(x, y, w, h,
                new net.minecraft.util.math.vector.Vector4f(2, 2, 2, 2), bg);
        polaroid.client.utils.render.DisplayUtils.drawRoundedRectOutline(x, y, w, h, 2, 0.7f, border);
        Fonts.otwindowsa.drawCenteredText(matrixStack, text, x + w / 2f, y + h / 2f - 3f, tc, 7f);
    }
    
    private Module getHoveredModule(int mouseX, int mouseY) {
        for (CategoryPanel panel : panels) {
            Module module = panel.getHoveredModule(mouseX, mouseY);
            if (module != null) {
                return module;
            }
        }
        return null;
    }
    
    private void renderColorPickers(MatrixStack matrixStack, int mouseX, int mouseY) {
        // Проходим по всем панелям и ищем открытые ColorPickerWindow
        for (CategoryPanel panel : panels) {
            panel.renderColorPickers(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int[] coords = transformMouseCoords(mouseX, mouseY);
        for (CategoryPanel panel : panels) {
            panel.mouseScrolled(coords[0], coords[1], delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Проверяем, идет ли биндинг настроек или модулей - если да, не закрываем GUI при ESC
        if (keyCode == 256 && (polaroid.client.ui.clickgui.layout.BindSettingComponent.isAnyBinding() || 
                               polaroid.client.ui.clickgui.layout.ModuleComponent.isAnyModuleBinding())) {
            // Передаем ESC в панели для обработки биндинга
            for (CategoryPanel panel : panels) {
                panel.keyPressed(keyCode, scanCode, modifiers);
            }
            return true; // Предотвращаем закрытие GUI
        }
        
        // Сначала обрабатываем поле поиска
        if (searchComponent.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        for (CategoryPanel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == 256) {
            mc.displayGuiScreen(null);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        // Обрабатываем ввод символов в поле поиска
        if (searchComponent.charTyped(character, modifiers)) {
            return true;
        }
        
        return super.charTyped(character, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int[] coords = transformMouseCoords(mouseX, mouseY);
        // Обрабатываем mouseReleased для ColorPickerWindow
        handleColorPickerReleased(coords[0], coords[1], button);

        // Обрабатываем отпускание в области панели конфигов (для будущих drag/scroll, если потребуется)
        
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(coords[0], coords[1], button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    private void handleColorPickerReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            panel.handleColorPickerReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        // Сбрасываем кэш blur при закрытии GUI
        polaroid.client.utils.performance.ClickGuiBlurCache.getInstance().reset();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] coords = transformMouseCoords(mouseX, mouseY);
        
        // Обработка клика по секции конфигов (кнопка + действия)
        if (handleConfigsClick(coords[0], coords[1], button)) {
            return true;
        }
        
        // ПЕРВЫМ делом проверяем клики по ColorPickerWindow
        // Это предотвращает клики по элементам под окном
        if (handleColorPickerClicks(coords[0], coords[1], button)) {
            return true;
        }
        
        // Сначала проверяем клик по полю поиска
        if (searchComponent.mouseClicked(coords[0], coords[1], button)) {
            return true;
        }
        
        for (CategoryPanel panel : panels) {
            panel.mouseClicked(coords[0], coords[1], button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private boolean handleColorPickerClicks(double mouseX, double mouseY, int button) {
        // Проходим по всем панелям и ищем открытые ColorPickerWindow
        for (CategoryPanel panel : panels) {
            if (panel.handleColorPickerClicks(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    private boolean handleConfigsClick(int mouseX, int mouseY, int button) {
        if (button != 0) return false;

        int fbWidth = mc.getMainWindow().getFramebufferWidth();
        int fbHeight = mc.getMainWindow().getFramebufferHeight();
        float screenWidth = (float)((double)fbWidth / 2.0);
        float screenHeight = (float)((double)fbHeight / 2.0);

        float buttonWidth = 90f;
        float buttonHeight = 14f;
        float buttonX = screenWidth - buttonWidth - 8f;
        float buttonY = screenHeight - buttonHeight - 8f;

        // Кнопка \"Конфиги\"
        if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth
                && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
            configsOpen = !configsOpen;
            return true;
        }

        if (!configsOpen) return false;

        float panelWidth = 190f;
        float panelX = screenWidth - panelWidth - 8f;
        float panelY = buttonY - configsPanelHeight - 6f;

        // Клики вне панели не обрабатываем
        if (mouseX < panelX || mouseX > panelX + panelWidth
                || mouseY < panelY || mouseY > panelY + configsPanelHeight) {
            return false;
        }

        float paddingX = 6f;
        float itemHeight = 11f;
        float buttonsY = panelY + configsPanelHeight - 18f;
        float actionWidth = (panelWidth - paddingX * 2 - 6f) / 4f;
        float bx = panelX + paddingX;

        // Проверяем, по какой кнопке действий кликнули
        if (mouseY >= buttonsY && mouseY <= buttonsY + itemHeight) {
            if (mouseX >= bx && mouseX <= bx + actionWidth) {
                applyConfigAction("load");
                return true;
            }
            bx += actionWidth + 2f;
            if (mouseX >= bx && mouseX <= bx + actionWidth) {
                applyConfigAction("save");
                return true;
            }
            bx += actionWidth + 2f;
            if (mouseX >= bx && mouseX <= bx + actionWidth) {
                applyConfigAction("create");
                return true;
            }
            bx += actionWidth + 2f;
            if (mouseX >= bx && mouseX <= bx + actionWidth) {
                applyConfigAction("delete");
                return true;
            }
        }

        // Клик по конкретному конфигу — делаем его активным
        float titleY = panelY + 6f;
        float listTop = titleY + 10f;
        float y = listTop;
        java.util.List<Config> configs = configStorage.getConfigs();
        for (Config cfg : configs) {
            if (mouseY >= y && mouseY <= y + itemHeight) {
                activeConfigName = cfg.getName();
                Config.currentConfig = activeConfigName;
                return true;
            }
            y += itemHeight;
            if (y > panelY + configsPanelHeight - 28f) {
                break;
            }
        }

        return false;
    }

    private void applyConfigAction(String action) {
        if (activeConfigName == null || activeConfigName.isEmpty()) {
            return;
        }

        if ("load".equalsIgnoreCase(action)) {
            configStorage.loadConfiguration(activeConfigName);
        } else if ("save".equalsIgnoreCase(action) || "create".equalsIgnoreCase(action)) {
            configStorage.saveConfiguration(activeConfigName);
        } else if ("delete".equalsIgnoreCase(action)) {
            java.io.File file = new java.io.File(configStorage.CONFIG_DIR, activeConfigName + ".cfg");
            if (file.exists()) {
                // Не трогаем системный autocfg
                if (!"autocfg".equalsIgnoreCase(activeConfigName)) {
                    file.delete();
                }
            }
        }
    }
}


