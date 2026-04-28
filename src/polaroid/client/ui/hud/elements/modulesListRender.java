package polaroid.client.ui.hud.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import polaroid.client.Polaroid;
import polaroid.client.events.EventDisplay;
import polaroid.client.events.EventUpdate;
import polaroid.client.modules.api.Category;
import polaroid.client.modules.api.Module;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.ui.hud.hudRender;
import polaroid.client.ui.hud.hudUpdater;
import polaroid.client.utils.math.StopWatch;
import polaroid.client.utils.render.ColorUtils;
import polaroid.client.utils.render.DisplayUtils;
import polaroid.client.utils.render.font.Fonts;
import net.minecraft.util.math.vector.Vector4f;
import ru.hogoshi.Animation;

import java.util.List;

public class modulesListRender implements hudRender, hudUpdater {

    private int lastIndex;

    List<Module> list;


    StopWatch stopWatch = new StopWatch();

    @Override
    public void update(EventUpdate e) {
        if (stopWatch.isReached(1000)) {
            list = Polaroid.getInstance().getFunctionRegistry().getSorted(Fonts.otwindowsa, 9 - 1.5f)
                    .stream()
                    .filter(m -> m.getCategory() != Category.Render)
                    .filter(m -> m.getCategory() != Category.Player)
                    .toList();
            stopWatch.reset();
        }
    }

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        
        float rounding = 6;
        float padding = 3f;
        float posX = 4;
        float posY = 4 + 28;
        int index = 0;

        if (list == null) return;

        for (Module f : list) {
            float fontSize = 6.5f;
            Animation anim = f.getAnimation();
            float value = (float) anim.getValue();
            String text = f.getName();
            float textWidth = Fonts.otwindowsa.getWidth(text, fontSize);

            if (value != 0) {
                float localFontSize = fontSize * value;
                float localTextWidth = textWidth * value;

                posY += (fontSize + padding * 2) * value;
                index++;
            }
        }
        index = 0;
        posY = 4 + 28;
        for (Module f : list) {
            float fontSize = 8f;
            Animation anim = f.getAnimation();
            anim.update();

            float value = (float) anim.getValue();

            String text = f.getName();
            float textWidth = Fonts.otwindowsa.getWidth(text, fontSize);

            if (value != 0) {
                float localFontSize = fontSize * value;
                float localTextWidth = textWidth * value;

                boolean iotwindowsairst = index == 0;
                boolean isLast = index == lastIndex;

                float localRounding = rounding;

                for (Module f2 : list.subList(list.indexOf(f) + 1, list.size())) { // predict next active module
                    if (f2.getAnimation().getValue() != 0) {
                        localRounding = isLast ? rounding : Math.min(textWidth - Fonts.otwindowsa.getWidth(f2.getName(), fontSize), rounding);
                        break;
                    }
                }
                
                Vector4f rectVec = new Vector4f(iotwindowsairst ? rounding : 0, isLast ? rounding : 0, iotwindowsairst ? rounding : 0, isLast ? rounding : localRounding);

                DisplayUtils.drawRoundedRect(posX, posY, localTextWidth + padding * 2, localFontSize + padding * 2, 2, ColorUtils.rgba(21, 21, 21, (int) (210 * value)));

                Fonts.otwindowsa.drawText(ms, f.getName(), posX + padding, posY + padding, ColorUtils.setAlpha(Theme.getColor(index), (int) (255 * value)), localFontSize);

                posY += (fontSize + padding * 2) * value;
                index++;
            }
        }

        lastIndex = index - 1;
    }
}


