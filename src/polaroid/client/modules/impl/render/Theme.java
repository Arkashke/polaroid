package polaroid.client.modules.impl.render;

import org.lwjgl.system.CallbackI;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.Setting;
import polaroid.client.modules.settings.impl.ColorSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.Polaroid;
import polaroid.client.ui.styles.Style;
import java.awt.Color;

@ModuleSystem(
        name = "ThemeSetting",
        type = Category.Render,
        server = ServerCategory.NO,
        description = "Позволяет менять тему клиента"
)
public class Theme extends Module {
    public static final ModeSetting choose = new ModeSetting("Тип цвета", "Заготовка", "Заготовка", "Свой");
    public static final ModeSetting THEME = new ModeSetting("Выбор Цвета", "Client", new String[]{"Client", "Snow", "Purple", "Custom"}).setVisible(() -> choose.is("Заготовка"));
    public static final SliderSetting speedColors = new SliderSetting("Скорость цвета", 10.0F, 0.0F, 20.0F, 1.0F);
    public static final ColorSetting color1 = (new ColorSetting("Первый цвет", -1)).setVisible(() -> choose.is("Заготовка") && THEME.is("Custom"));
    public static final ColorSetting color2 = (new ColorSetting("Второй цвет", -1)).setVisible(() -> choose.is("Заготовка") && THEME.is("Custom"));

    public Theme() {
        this.toggle();
        this.addSettings(new Setting[]{THEME, speedColors, color1, color2});
        updateStyleManager();
    }
    
    private void updateStyleManager() {
        try {
            Style currentStyle = Polaroid.getInstance().getStyleManager().getCurrentStyle();
            
            if (THEME.is("Client")) {
                currentStyle.setFirstColor(new Color(0xE7F5DC));
                currentStyle.setSecondColor(new Color(0x728156));
            } else if (THEME.is("Snow")) {
                currentStyle.setFirstColor(new Color(0xCFE1B9));
                currentStyle.setSecondColor(new Color(0x98A77C));
            } else if (THEME.is("Purple")) {
                currentStyle.setFirstColor(new Color(0xB6C99B));
                currentStyle.setSecondColor(new Color(0x88976C));
            } else if (THEME.is("Custom")) {
                currentStyle.setFirstColor(new Color(color1.get()));
                currentStyle.setSecondColor(new Color(color2.get()));
            }
        } catch (Exception e) {}
    }

    public static int getColor(int index) {
        return Temka(index + 16);
    }

    public static int getColor(int index, float mult) {
        return Temka((int)((float)index * mult + 16.0F) + 16);
    }

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColorUtils.gradient(firstColor, secondColor, (int)((float)index * mult), 10);
    }

    public static int Temka(int index) {
        updateColors();
        
        if (THEME.is("Client")) {
            return ColorUtils.gradient(ColorUtils.rgb(231, 245, 220), ColorUtils.rgb(114, 129, 86), index + 16, 12);
        } else if (THEME.is("Snow")) {
            return ColorUtils.gradient(ColorUtils.rgb(207, 225, 185), ColorUtils.rgb(152, 167, 124), index + 16, 12);
        } else if (THEME.is("Purple")) {
            return ColorUtils.gradient(ColorUtils.rgb(182, 201, 155), ColorUtils.rgb(136, 151, 108), index + 16, 12);
        } else {
            return THEME.is("Custom") ? ColorUtils.gradient((Integer)color1.get(), (Integer)color2.get(), index, 12) : index * 16;
        }
    }
    
    private static void updateColors() {
        try {
            Style currentStyle = Polaroid.getInstance().getStyleManager().getCurrentStyle();
            
            if (THEME.is("Client")) {
                currentStyle.setFirstColor(new Color(0xE7F5DC));
                currentStyle.setSecondColor(new Color(0x728156));
            } else if (THEME.is("Snow")) {
                currentStyle.setFirstColor(new Color(0xCFE1B9));
                currentStyle.setSecondColor(new Color(0x98A77C));
            } else if (THEME.is("Purple")) {
                currentStyle.setFirstColor(new Color(0xB6C99B));
                currentStyle.setSecondColor(new Color(0x88976C));
            } else if (THEME.is("Custom")) {
                currentStyle.setFirstColor(new Color(color1.get()));
                currentStyle.setSecondColor(new Color(color2.get()));
            }
        } catch (Exception e) {}
    }
}


