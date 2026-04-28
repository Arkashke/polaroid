package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ColorSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import polaroid.client.modules.settings.impl.ModeSetting;
import polaroid.client.modules.settings.impl.SliderSetting;
import polaroid.client.ui.hud.elements.*;
import polaroid.client.ui.styles.StyleManager;
import polaroid.client.utils.drag.Dragging;
import polaroid.client.utils.render.ColorUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleSystem(name = "HUD", type = Category.Render, server = ServerCategory.NO, description = "Отображение элементов интерфейса")
public class InterFace extends Module {
    public ModeSetting hudType = new ModeSetting("Тип HUD", "Wexside", "Wexside", "Светлый", "Темный");
    public BooleanSetting brackets = new BooleanSetting("Скобки", true);
    public BooleanSetting hudOutline = new BooleanSetting("Обводка HUD", true);
    public ModeSetting outlineColorMode = new ModeSetting("Цвет обводки", "Клиент", "Клиент", "Свой").setVisible(() -> hudOutline.get());
    public ColorSetting customOutlineColor = new ColorSetting("Свой цвет обводки", -1).setVisible(() -> hudOutline.get() && outlineColorMode.is("Свой"));
    public ModeSetting coordsStyle = new ModeSetting("Стиль координат", "Современный", "Современный", "Упрощенный");
    private final ModeListSetting elements = new ModeListSetting("Элементы",
            new BooleanSetting("Логотип", true),
            new BooleanSetting("Местоположение", true),
            new BooleanSetting("Активные Эффекты", true),
            new BooleanSetting("Модерация онлайн", true),
            new BooleanSetting("Активные привязки", true),
            new BooleanSetting("Состояние таргета", true),
            new BooleanSetting("Броня", true),
            new BooleanSetting("Задержка на предметы", true),
            new BooleanSetting("Timer", true)
    );
    final waterMarkRender watermarkRenderer;
    final gpsCoordsRender coordsRenderer;
    final potionListRender potionRenderer;

    final hotkeysRender keyBindRenderer;
    final targetHudRender targetInfoRenderer;
    final armorStatusRender armorRenderer;
    final staffListRender staffListRenderer;
    final cooldownRender cooldownsRenderer;
    final timerRender timerRenderer;

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        if (elements.getValueByName("Модерация онлайн").get()) staffListRenderer.update(e);
    }


    @Subscribe
    private void onDisplay(EventDisplay e) {
        if (mc.gameSettings.showDebugInfo || e.getType() != EventDisplay.Type.POST) {
            return;
        }

        if (elements.getValueByName("Местоположение").get()) coordsRenderer.render(e);
        if (elements.getValueByName("Активные Эффекты").get()) potionRenderer.render(e);
        if (elements.getValueByName("Логотип").get()) watermarkRenderer.render(e);
        if (elements.getValueByName("Активные привязки").get()) keyBindRenderer.render(e);
        if (elements.getValueByName("Модерация онлайн").get()) staffListRenderer.render(e);
        if (elements.getValueByName("Состояние таргета").get()) targetInfoRenderer.render(e);
        if (elements.getValueByName("Задержка на предметы").get()) cooldownsRenderer.render(e);
        if (elements.getValueByName("Timer").get()) timerRenderer.render(e);

    }

    public InterFace() {
        watermarkRenderer = new waterMarkRender();
        Dragging coordsDrag = Polaroid.getInstance().createDrag(this, "Coordinates", 5, 5);
        coordsRenderer = new gpsCoordsRender(coordsDrag);
        // Potions теперь фиксированы посередине слева, не нужен Dragging
        potionRenderer = new potionListRender();
        armorRenderer = new armorStatusRender();
        Dragging keyBinds = Polaroid.getInstance().createDrag(this, "KeyBinds", 185, 5);
        Dragging dragging = Polaroid.getInstance().createDrag(this, "TargetInterFace", 74, 128);
        Dragging staffList = Polaroid.getInstance().createDrag(this, "StaffList", 96, 5);
        Dragging cooldowns = Polaroid.getInstance().createDrag(this, "CoolDowns", 300,5);
        Dragging timerDrag = Polaroid.getInstance().createDrag(this, "Timer", 300, 50);
        keyBindRenderer = new hotkeysRender(keyBinds);
        staffListRenderer = new staffListRender(staffList);
        targetInfoRenderer = new targetHudRender(dragging);
        cooldownsRenderer = new cooldownRender(cooldowns);
        timerRenderer = new timerRender(timerDrag);
        addSettings(hudType, elements, brackets, hudOutline, outlineColorMode, customOutlineColor, coordsStyle);
    }
    
    public int getBackgroundColor() {
        return hudType.is("Светлый") ? ColorUtils.rgba(240, 240, 245, 150) : ColorUtils.rgba(20, 20, 25, 200);
    }
    
    public int getBorderColor() {
        return hudType.is("Светлый") ? ColorUtils.rgba(200, 200, 210, 60) : ColorUtils.rgba(60, 60, 70, 100);
    }
    
    public int getTextColor() {
        return hudType.is("Светлый") ? ColorUtils.rgba(30, 30, 35, 255) : ColorUtils.rgba(255, 255, 255, 255);
    }
    
    public int getSeparatorColor() {
        return hudType.is("Светлый") ? ColorUtils.rgba(60, 60, 60, 150) : ColorUtils.rgba(200, 200, 200, 150);
    }
    
    // Метод для получения цвета обводки HUD с градиентом
    public int getHudOutlineColor(int index) {
        if (!hudOutline.get()) {
            return 0; // Прозрачный если обводка выключена
        }
        
        // Если выбран свой цвет
        if (outlineColorMode.is("Свой")) {
            return ColorUtils.setAlpha(customOutlineColor.get(), 200);
        }
        
        // Режим "Клиент" - используем градиент темы
        // Светлая тема = темная обводка с градиентом
        // Темная тема = светлая обводка с градиентом
        if (hudType.is("Светлый")) {
            // Темная обводка с градиентом темы
            return ColorUtils.setAlpha(Theme.getColor(index), 180);
        } else {
            // Светлая обводка с градиентом темы
            return ColorUtils.setAlpha(Theme.getColor(index), 200);
        }
    }

    public static int getColor(int index) {
        StyleManager styleManager = Polaroid.getInstance().getStyleManager();
        return ColorUtils.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 16, 10);
    }

    public static int getColor(int index, float mult) {
        StyleManager styleManager = Polaroid.getInstance().getStyleManager();
        return ColorUtils.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), (int) (index * mult), 10);
    }

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColorUtils.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }
}

