package polaroid.client.ui.clickgui.layout;

import polaroid.client.modules.api.Module;
import polaroid.client.modules.settings.Setting;
import polaroid.client.modules.settings.impl.*;

/**
 * Базовый класс для компонентов настроек
 */
public abstract class SettingComponent extends UIComponent {
    
    protected Setting<?> setting;
    protected Module module;
    
    public SettingComponent(Setting<?> setting, Module module) {
        this.setting = setting;
        this.module = module;
    }
    
    /**
     * Проверка видимости настройки
     */
    protected boolean isVisible() {
        if (setting.visible == null) return true;
        try {
            Boolean visible = (Boolean) setting.visible.get();
            return Boolean.TRUE.equals(visible);
        } catch (Exception e) {
            return true;
        }
    }
    
    @Override
    public float measure(float availableWidth) {
        if (!isVisible()) {
            measuredHeight = 0;
            return 0;
        }
        measuredHeight = calculateHeight(availableWidth);
        return measuredHeight;
    }
    
    /**
     * Вычислить высоту компонента
     */
    protected abstract float calculateHeight(float availableWidth);
    
    /**
     * Фабричный метод для создания компонента настройки
     */
    public static SettingComponent create(Setting<?> setting, Module module) {
        if (setting instanceof BooleanSetting) {
            return new BooleanSettingComponent((BooleanSetting) setting, module);
        } else if (setting instanceof SliderSetting) {
            return new SliderSettingComponent((SliderSetting) setting, module);
        } else if (setting instanceof ModeSetting) {
            return new ModeSettingComponent((ModeSetting) setting, module);
        } else if (setting instanceof ModeListSetting) {
            return new MultiSettingComponent((ModeListSetting) setting, module);
        } else if (setting instanceof BindSetting) {
            return new BindSettingComponent((BindSetting) setting, module);
        } else if (setting instanceof ColorSetting) {
            return new ColorSettingComponent((ColorSetting) setting, module);
        }
        return null;
    }
}


