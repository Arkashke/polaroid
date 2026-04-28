package polaroid.client.modules.impl.render;

import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.SliderSetting;

@ModuleSystem(name = "Scoreboard", type = Category.Render, server = ServerCategory.NO, description = "Настройки скорборда")
public class Scoreboard extends Module {

    private final BooleanSetting hideNumbers = new BooleanSetting("Скрыть номера", false);
    private final BooleanSetting customPosition = new BooleanSetting("Своя позиция", false);
    private final SliderSetting posX = new SliderSetting("Позиция X", 0, -500, 500, 1)
            .setVisible(() -> customPosition.get());
    private final SliderSetting posY = new SliderSetting("Позиция Y", 0, -500, 500, 1)
            .setVisible(() -> customPosition.get());

    public Scoreboard() {
        addSettings(hideNumbers, customPosition, posX, posY);
    }

    public boolean shouldHideNumbers() {
        return isState() && hideNumbers.get();
    }

    public boolean hasCustomPosition() {
        return isState() && customPosition.get();
    }

    public int getCustomX() {
        return posX.get().intValue();
    }

    public int getCustomY() {
        return posY.get().intValue();
    }
}


