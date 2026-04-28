package polaroid.client.modules.impl.render;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "ClientSounds", type = Category.Misc, server = ServerCategory.NO, description = "При включении модуля воспроизводит звук")
public class ClientSounds extends Module {

    public ModeSetting mode = new ModeSetting("Тип", "Стандарт", "Стандарт", "Пузырь", "Celestial", "Nursultan", "Windows 10", "Slide", "Droplet", "VL");
    public SliderSetting volume = new SliderSetting("Громкость", 60.0f, 0.0f, 100.0f, 1.0f);

    public ClientSounds() {
        addSettings(mode, volume);
        toggle();
    }

    public String getFileName(boolean state) {
        switch (mode.get()) {
            case "Стандарт" -> {
                return state ? "enable" : "disable";
            }
            case "Пузырь" -> {
                return state ? "enableBubbles" : "disableBubbles";
            }
            case "Celestial" -> {
                return state ? "popenable" : "popdisable";
            }
            case "Nursultan" -> {
                return state ? "heavyenable" : "heavydisable";
            }
            case "Windows 10" -> {
                return state ? "winenable" : "windisable";
            }
            case "Droplet" -> {
                return state ? "dropletenable" : "dropletdisable";
            }
            case "Slide" -> {
                return state ? "slideenable" : "slidedisable";
            }
            case "VL" -> {
                return state ? "enablevl" : "disablevl";
            }
        }
        return "";
    }
}



