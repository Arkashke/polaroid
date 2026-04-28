package polaroid.client.ui.clickgui.layout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Vector4f;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.Setting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import polaroid.client.utils.math.MathUtil;
import polaroid.client.Polaroid;

/**
 * Компонент модуля с автоматическим расчетом высоты
 */
public class ModuleComponent extends UIComponent {
    
    private static final float MODULE_HEIGHT = 18f;
    private static final float SETTING_HEIGHT = 12f;
    private static ModuleComponent currentlyBinding = null;
    
    private final Module module;
    private final VerticalContainer settingsContainer;
    private float expandAnimation = 0f;
    private boolean isBinding = false;
    
    public ModuleComponent(Module module) {
        this.module = module;
        this.settingsContainer = new VerticalContainer(1.5f); // Уменьшил spacing с 0f до 1.5f для минимального отступа
        
        // Создаем компоненты для настроек
        rebuildSettings();
    }
    
    private void rebuildSettings() {
        settingsContainer.clearChildren();
        
        for (Setting<?> setting : module.getSettings()) {
            try {
                SettingComponent settingComp = SettingComponent.create(setting, module);
                if (settingComp != null) {
                    settingsContainer.addChild(settingComp);
                }
            } catch (Exception e) {
                // Игнорируем ошибки создания компонентов
            }
        }
    }
    
    @Override
    public float measure(float availableWidth) {
        float totalHeight = MODULE_HEIGHT;
        
        if (module.expanded && !module.getSettings().isEmpty()) {
            // Измеряем высоту настроек
            float settingsHeight = settingsContainer.measure(availableWidth);
            totalHeight += settingsHeight * expandAnimation;
        }
        
        measuredHeight = totalHeight;
        return measuredHeight;
    }
    
    @Override
    public void layout(float x, float y, float width) {
        super.layout(x, y, width);
        
        if (module.expanded && expandAnimation > 0.01f) {
            // Размещаем контейнер с настройками
            settingsContainer.layout(x, y + MODULE_HEIGHT, width);
        }
    }
    
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        // ОПТИМИЗАЦИЯ: Throttling анимации
        if (polaroid.client.utils.performance.ClickGuiOptimizer.getInstance()
                .shouldUpdateAnimation(module.getName())) {
            expandAnimation = MathUtil.fast(expandAnimation, module.expanded ? 1f : 0f, 10f);
        }
        
        // Если анимация изменилась - пересчитываем layout
        if (needsLayout || Math.abs(expandAnimation - (module.expanded ? 1f : 0f)) > 0.01f) {
            invalidate();
        }
        
        int activColor = Polaroid.getInstance().getStyleManager().getCurrentStyle().getFirstColor().getRGB();
        
        // Фон модуля - используем полную ширину
        if (module.isState()) {
            DisplayUtils.drawRoundedRect(x, y, width, MODULE_HEIGHT,
                    new Vector4f(3.5f, 3.5f, 3.5f, 3.5f),
                    ColorUtils.setAlpha(activColor, 80));
            DisplayUtils.drawRoundedRectOutline(x, y, width, MODULE_HEIGHT,
                    3.5f, 1.5f, ColorUtils.setAlpha(activColor, 255));
        } else {
            DisplayUtils.drawRoundedRect(x, y, width, MODULE_HEIGHT,
                    new Vector4f(3.5f, 3.5f, 3.5f, 3.5f),
                    ColorUtils.rgba(58, 58, 58, 50));
            DisplayUtils.drawRoundedRectOutline(x, y, width, MODULE_HEIGHT,
                    3.5f, 1.5f, ColorUtils.rgba(80, 80, 80, 180));
        }
        
        // Название модуля - ОПТИМИЗАЦИЯ: кэшируем ширину
        String name = isBinding ? "[...]" : module.getName();
        int textColor = module.isState() ? 
                ColorUtils.rgba(255, 255, 255, 255) : 
                ColorUtils.rgba(180, 180, 180, 255);
        Fonts.otwindowsa.drawText(stack, name, x + 5f, y + 5f, textColor, 8);
        
        // Иконка настроек
        if (!module.getSettings().isEmpty()) {
            Fonts.excellenticon.drawText(stack, "f", x + width - 12, y + 5.5f,
                    module.isState() ? ColorUtils.setAlpha(activColor, 255) :
                            ColorUtils.rgba(140, 140, 140, 128), 8);
        }
        
        // Рендерим настройки если раскрыто
        if (module.expanded && expandAnimation > 0.01f) {
            settingsContainer.render(stack, mouseX, mouseY);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Запрещаем клик по модулю ClickGui
        if (module.getName().equals("ClickGui")) {
            // Разрешаем только раскрытие настроек (ПКМ)
            if (mouseY >= y && mouseY <= y + MODULE_HEIGHT && button == 1) {
                module.expanded = !module.expanded;
                invalidate();
                return true;
            }
            // Клик по настройкам если раскрыто
            if (module.expanded && expandAnimation > 0.5f) {
                return settingsContainer.mouseClicked(mouseX, mouseY, button);
            }
            return false;
        }
        
        // Клик по модулю
        if (mouseY >= y && mouseY <= y + MODULE_HEIGHT) {
            if (button == 0) {
                module.toggle();
                return true;
            } else if (button == 1) {
                module.expanded = !module.expanded;
                invalidate();
                return true;
            } else if (button == 2) {
                // Отменяем предыдущий модуль в режиме binding
                if (currentlyBinding != null && currentlyBinding != this) {
                    currentlyBinding.isBinding = false;
                }
                isBinding = !isBinding;
                if (isBinding) {
                    currentlyBinding = this;
                } else {
                    currentlyBinding = null;
                }
                return true;
            }
        }
        
        // Клик по настройкам
        if (module.expanded && expandAnimation > 0.5f) {
            return settingsContainer.mouseClicked(mouseX, mouseY, button);
        }
        
        return false;
    }
    
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (module.expanded) {
            settingsContainer.mouseReleased(mouseX, mouseY, button);
        }
    }
    
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isBinding) {
            if (keyCode == 261 || keyCode == 259 || keyCode == 256) {
                module.setBind(0);
            } else {
                module.setBind(keyCode);
            }
            isBinding = false;
            if (currentlyBinding == this) {
                currentlyBinding = null;
            }
            // Не распространяем событие дальше чтобы не закрыть GUI
            return;
        }
        
        if (module.expanded) {
            settingsContainer.keyPressed(keyCode, scanCode, modifiers);
        }
    }
    
    public static boolean isAnyModuleBinding() {
        return currentlyBinding != null;
    }
    
    public boolean isBinding() {
        return isBinding;
    }
    
    public Module getModule() {
        return module;
    }
    
    public void renderColorPickers(MatrixStack stack, int mouseX, int mouseY) {
        // Проходим по всем настройкам и рендерим открытые ColorPickerWindow
        if (module.expanded) {
            for (UIComponent child : settingsContainer.getChildren()) {
                if (child instanceof ColorSettingComponent) {
                    ColorSettingComponent colorComp = (ColorSettingComponent) child;
                    if (colorComp.getColorPickerWindow().isVisible()) {
                        colorComp.getColorPickerWindow().render(stack, mouseX, mouseY);
                    }
                }
            }
        }
    }
    
    public boolean handleColorPickerClicks(double mouseX, double mouseY, int button) {
        // Проходим по всем настройкам и проверяем клики по ColorPickerWindow
        if (module.expanded) {
            for (UIComponent child : settingsContainer.getChildren()) {
                if (child instanceof ColorSettingComponent) {
                    ColorSettingComponent colorComp = (ColorSettingComponent) child;
                    if (colorComp.getColorPickerWindow().isVisible()) {
                        if (colorComp.getColorPickerWindow().mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public void handleColorPickerReleased(double mouseX, double mouseY, int button) {
        // Проходим по всем настройкам и обрабатываем mouseReleased для ColorPickerWindow
        if (module.expanded) {
            for (UIComponent child : settingsContainer.getChildren()) {
                if (child instanceof ColorSettingComponent) {
                    ColorSettingComponent colorComp = (ColorSettingComponent) child;
                    if (colorComp.getColorPickerWindow().isVisible()) {
                        colorComp.getColorPickerWindow().mouseReleased(mouseX, mouseY, button);
                    }
                }
            }
        }
    }
}


