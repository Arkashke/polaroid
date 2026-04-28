package polaroid.client.modules.impl.render;

import com.google.common.eventbus.Subscribe;

import polaroid.client.events.EventCancelOverlay;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.api.ModuleSystem;
import polaroid.client.modules.api.ServerCategory;
import polaroid.client.modules.settings.impl.BooleanSetting;
import polaroid.client.modules.settings.impl.ModeListSetting;
import net.minecraft.potion.Effects;
@ModuleSystem(name = "Removals", type = Category.Render, description = "Убирает какие либо эффекты", server = ServerCategory.NO)
public class NoRender extends Module {

    public ModeListSetting element = new ModeListSetting("Удалять",
            new BooleanSetting("Огонь на экране", true),
            new BooleanSetting("Линия босса", false),
            new BooleanSetting("Анимация тотема", true),
            new BooleanSetting("Тайтлы", false),
            new BooleanSetting("Таблица", false),
            new BooleanSetting("Туман", true),
            new BooleanSetting("Тряску камеры", true),
            new BooleanSetting("Плохие эффекты", true),
            new BooleanSetting("Дождь", true),
            new BooleanSetting("Камера клип", true),
            new BooleanSetting("Броня", false),
            new BooleanSetting("Плащ", false),
            new BooleanSetting("Трава", true),
            new BooleanSetting("Эффект свечения", true),
            new BooleanSetting("Эффект воды", true)
    );

    public NoRender() {
        addSettings(element);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        handleEventUpdate(e);
    }

    @Subscribe
    private void onEventCancelOverlay(EventCancelOverlay e) {
        handleEventOverlaysRender(e);
    }

    private void handleEventOverlaysRender(EventCancelOverlay event) {
        boolean cancelOverlay = switch (event.overlayType) {
            case FIRE_OVERLAY -> element.getValueByName("Огонь на экране").get();
            case BOSS_LINE -> element.getValueByName("Линия босса").get();
            case SCOREBOARD -> element.getValueByName("Таблица").get();
            case TITLES -> element.getValueByName("Тайтлы").get();
            case TOTEM -> element.getValueByName("Анимация тотема").get();
            case FOG -> element.getValueByName("Туман").get();
            case HURT -> element.getValueByName("Тряску камеры").get();
            case UNDER_WATER -> element.getValueByName("Эффект воды").get();
            case CAMERA_CLIP -> element.getValueByName("Камера клип").get();
            case TRAVA -> element.getValueByName("Трава").get();
            case ARMOR -> element.getValueByName("Броня").get();
        };

        if (cancelOverlay) {
            event.cancel();
        }
    }

    private void handleEventUpdate(EventUpdate event) {
        boolean isRaining = mc.world.isRaining() && element.getValueByName("Дождь").get();

        boolean hasEffects = (mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isPotionActive(Effects.NAUSEA)) && element.getValueByName("Плохие эффекты").get();

        if (isRaining) {
            mc.world.setRainStrength(0);
            mc.world.setThunderStrength(0);
        }

        if (hasEffects) {
            mc.player.removePotionEffect(Effects.NAUSEA);
            mc.player.removePotionEffect(Effects.BLINDNESS);
        }
    }
}


