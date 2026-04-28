package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import polaroid.client.events.EventUpdate;
import polaroid.client.events.EventPacket;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ColorSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import net.minecraft.network.play.server.SUpdateTimePacket;

@ModuleSystem(name = "CustomWorld", type = Category.Render, server = ServerCategory.NO, description = "Настройки мира и игрока")
public class CustomWorld extends Module {

    public static ModeListSetting options = new ModeListSetting("Опции",
            new BooleanSetting("Маленький игрок", false),
            new BooleanSetting("Смена времени", false),
            new BooleanSetting("Убрать растения", false),
            new BooleanSetting("Кастомный туман", false)
    );

    public static ModeSetting timeMode = new ModeSetting("Время суток", "Утро", "Утро", "День", "Закат", "Ночь").setVisible(() -> options.getValueByName("Смена времени").get());
    public static ModeSetting mode = new ModeSetting("Режим тумана", "Клиент", "Клиент", "Свой").setVisible(() -> options.getValueByName("Кастомный туман").get());
    public static ColorSetting colorFog = new ColorSetting("Цвет тумана", -1).setVisible(() -> options.getValueByName("Кастомный туман").get() && mode.is("Свой"));
    public static SliderSetting fogDensity = new SliderSetting("Плотность тумана", 100f, 0f, 200f, 1f).setVisible(() -> options.getValueByName("Кастомный туман").get());
    
    public static boolean child;

    public CustomWorld() {
        addSettings(options, timeMode, mode, colorFog, fogDensity);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if ((!isEnabled("Маленький игрок") &&
                !isEnabled("Смена времени") &&
                !isEnabled("Убрать растения") &&
                !isEnabled("Кастомный туман")
        )) {
            toggle();
        }

        if (isEnabled("Маленький игрок")) {
            child = true;
        } else {
            child = false;
        }
        
        // Принудительно устанавливаем статичное время на клиенте
        if (isEnabled("Смена времени") && mc.world != null) {
            long targetTime;
            switch (timeMode.get()) {
                case "Утро":
                    targetTime = 1000L;
                    break;
                case "День":
                    targetTime = 6000L;
                    break;
                case "Закат":
                    targetTime = 12000L;
                    break;
                case "Ночь":
                    targetTime = 13000L;
                    break;
                default:
                    targetTime = 6000L;
            }
            // Принудительно устанавливаем время мира каждый тик
            mc.world.getWorldInfo().setDayTime(targetTime);
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (!isEnabled("Смена времени")) return;
        
        if (e.getPacket() instanceof SUpdateTimePacket p) {
            long targetTime;
            switch (timeMode.get()) {
                case "Утро":
                    targetTime = 1000L;
                    break;
                case "День":
                    targetTime = 6000L;
                    break;
                case "Закат":
                    targetTime = 12000L;
                    break;
                case "Ночь":
                    targetTime = 13000L;
                    break;
                default:
                    targetTime = 6000L;
            }
            // Устанавливаем статичное время - солнце не будет двигаться
            p.worldTime = targetTime;
            p.totalWorldTime = targetTime;
        }
    }

    public boolean isEnabled(String name) {
        return options.getValueByName(name).get();
    }

    @Override
    public boolean onDisable() {
        super.onDisable();
        child = false;
        return false;
    }
}


